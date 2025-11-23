package com.minegolem.cGM.listener;

import com.minegolem.cGM.manager.PotionStackManager;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public class PotionStackListener implements Listener {

    private final JavaPlugin plugin;
    private static final int MAX_POTION_STACK = 16;

    public PotionStackListener(JavaPlugin plugin) {
        this.plugin = plugin;
        startPotionCleanupTask();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.isCancelled() || !(event.getWhoClicked() instanceof Player player)) return;
        if (player.getGameMode() == GameMode.CREATIVE || event.getInventory().getType() == InventoryType.BREWING) return;

        ItemStack cursor = event.getCursor();
        ItemStack current = event.getCurrentItem();

        if (cursor != null && current != null && PotionStackManager.canStack(cursor, current)) {
            handlePotionCombine(event, cursor, current);
            return;
        }

        if (event.isShiftClick() && current != null && PotionStackManager.isPotionType(current.getType())) {
            handleShiftClick(event, current);
        }

        schedulePotionLoreRefresh(player, event.getInventory());
    }

    private void handlePotionCombine(InventoryClickEvent event, ItemStack cursor, ItemStack current) {
        event.setCancelled(true);

        int total = cursor.getAmount() + current.getAmount();
        if (total <= MAX_POTION_STACK) {
            current.setAmount(total);
            PotionStackManager.updateStackLore(current);
            event.setCursor(null);
        } else {
            current.setAmount(MAX_POTION_STACK);
            cursor.setAmount(total - MAX_POTION_STACK);
            PotionStackManager.updateStackLore(current);
            PotionStackManager.updateStackLore(cursor);
            event.setCursor(cursor);
        }

        forceInventoryUpdate(event.getInventory());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.isCancelled() || !(event.getWhoClicked() instanceof Player player)) return;
        if (player.getGameMode() == GameMode.CREATIVE || event.getInventory().getType() == InventoryType.BREWING) return;

        ItemStack draggedItem = event.getOldCursor();
        if (draggedItem == null || !PotionStackManager.isPotionType(draggedItem.getType())) return;

        for (Integer slot : event.getInventorySlots()) {
            ItemStack slotItem = event.getView().getItem(slot);
            if (slotItem != null && PotionStackManager.canStack(draggedItem, slotItem)) {
                event.setCancelled(true);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        performDragStacking(event, draggedItem);
                    }
                }.runTaskLater(plugin, 1L);
                break;
            }
        }
    }

    private void performDragStacking(InventoryDragEvent event, ItemStack draggedItem) {
        Map<Integer, Integer> allocation = allocatePotionDistribution(event, draggedItem);
        applyPotionDistribution(event, draggedItem, allocation);
    }

    private Map<Integer, Integer> allocatePotionDistribution(InventoryDragEvent event, ItemStack draggedItem) {
        Map<Integer, Integer> distribution = new HashMap<>();
        int remaining = draggedItem.getAmount();

        for (Integer slot : event.getInventorySlots()) {
            ItemStack slotItem = event.getView().getItem(slot);
            if (slotItem != null && PotionStackManager.canStack(draggedItem, slotItem)) {
                int maxAdd = MAX_POTION_STACK - slotItem.getAmount();
                int toAdd = Math.min(maxAdd, remaining / event.getInventorySlots().size() + (remaining % event.getInventorySlots().size() > 0 ? 1 : 0));
                if (toAdd > 0) {
                    distribution.put(slot, toAdd);
                    remaining -= toAdd;
                }
            }
        }
        return distribution;
    }

    private void applyPotionDistribution(InventoryDragEvent event, ItemStack draggedItem, Map<Integer, Integer> distribution) {
        distribution.forEach((slot, amount) -> {
            ItemStack slotItem = event.getView().getItem(slot);
            slotItem.setAmount(slotItem.getAmount() + amount);
            PotionStackManager.updateStackLore(slotItem);
            event.getView().setItem(slot, slotItem);
        });

        if (draggedItem.getAmount() > 0) {
            draggedItem.setAmount(draggedItem.getAmount());
            PotionStackManager.updateStackLore(draggedItem);
            event.getWhoClicked().setItemOnCursor(draggedItem);
        } else {
            event.getWhoClicked().setItemOnCursor(null);
        }

        scheduleInventoryRefresh(event.getInventory());
    }

    private void handleShiftClick(InventoryClickEvent event, ItemStack item) {
        Player player = (Player) event.getWhoClicked();

        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack slotItem = player.getInventory().getItem(i);
            if (slotItem != null && PotionStackManager.canStack(item, slotItem) && slotItem.getAmount() < MAX_POTION_STACK) {
                event.setCancelled(true);
                int move = Math.min(item.getAmount(), MAX_POTION_STACK - slotItem.getAmount());
                slotItem.setAmount(slotItem.getAmount() + move);
                item.setAmount(item.getAmount() - move);

                PotionStackManager.updateStackLore(slotItem);
                if (item.getAmount() > 0) PotionStackManager.updateStackLore(item);
                if (item.getAmount() <= 0) event.setCurrentItem(null);

                forceInventoryUpdate(event.getInventory());
                return;
            }
        }
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        if (player.getGameMode() == GameMode.CREATIVE || event.getInventory().getType() == InventoryType.BREWING) return;

        new BukkitRunnable() {
            @Override
            public void run() {
                normalizePotionInventory(player);
            }
        }.runTaskLater(plugin, 1L);
    }

    @EventHandler
    public void onPickup(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE) return;

        ItemStack item = event.getItem().getItemStack();
        if (!PotionStackManager.isPotionType(item.getType())) return;

        item = PotionStackManager.normalizePotion(item);
        event.getItem().setItemStack(item);

        for (ItemStack slot : player.getInventory().getContents()) {
            if (slot != null && PotionStackManager.canStack(item, slot) && slot.getAmount() < MAX_POTION_STACK) {
                int add = Math.min(item.getAmount(), MAX_POTION_STACK - slot.getAmount());
                slot.setAmount(slot.getAmount() + add);
                item.setAmount(item.getAmount() - add);

                PotionStackManager.updateStackLore(slot);
                if (item.getAmount() <= 0) {
                    event.setCancelled(true);
                    event.getItem().remove();
                    return;
                }
            }
        }
    }

    public void startPotionCleanupTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.getOnlinePlayers().forEach(player -> {
                    cleanupInventory(p -> true, player.getInventory());
                    if (player.getOpenInventory() != null)
                        cleanupInventory(p -> true, player.getOpenInventory().getTopInventory());
                });
            }
        }.runTaskTimer(plugin, 20L, 40L);
    }

    private void cleanupInventory(java.util.function.Predicate<ItemStack> filter, Inventory inventory) {
        if (inventory == null || inventory.getType() == InventoryType.BREWING) return;

        for (ItemStack item : inventory.getContents()) {
            if (item != null && PotionStackManager.isPotionType(item.getType()) && item.getAmount() == 1) {
                PotionStackManager.updateStackLore(item);
            }
        }
    }

    private void normalizePotionInventory(Player player) {
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item != null) {
                player.getInventory().setItem(i, PotionStackManager.normalizePotion(item));
            }
        }
        player.updateInventory();
    }

    private void schedulePotionLoreRefresh(Player player, Inventory inventory) {
        new BukkitRunnable() {
            @Override
            public void run() {
                updatePotionLore(player, inventory);
            }
        }.runTaskLater(plugin, 2L);
    }

    private void updatePotionLore(Player player, Inventory inventory) {
        updateInventoryPotions(player.getInventory());
        if (inventory != player.getInventory()) updateInventoryPotions(inventory);
        ItemStack cursor = player.getItemOnCursor();
        if (cursor != null) PotionStackManager.forceUpdateStackLore(cursor);
        player.updateInventory();
    }

    private void updateInventoryPotions(Inventory inventory) {
        for (ItemStack item : inventory.getContents()) {
            if (item != null) PotionStackManager.forceUpdateStackLore(item);
        }
    }

    private void forceInventoryUpdate(Inventory inventory) {
        new BukkitRunnable() {
            @Override
            public void run() {
                inventory.getViewers().forEach(viewer -> {
                    if (viewer instanceof Player player) {
                        player.updateInventory();
                        int original = inventory.getMaxStackSize();
                        inventory.setMaxStackSize(original + 1);

                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                inventory.setMaxStackSize(original);
                                player.updateInventory();
                            }
                        }.runTaskLater(plugin, 1L);
                    }
                });
            }
        }.runTaskLater(plugin, 1L);
    }

    private void scheduleInventoryRefresh(Inventory inventory) {
        new BukkitRunnable() {
            @Override
            public void run() {
                inventory.getViewers().forEach(viewer -> {
                    if (viewer instanceof Player p) p.updateInventory();
                });
            }
        }.runTaskLater(plugin, 2L);
    }
}
