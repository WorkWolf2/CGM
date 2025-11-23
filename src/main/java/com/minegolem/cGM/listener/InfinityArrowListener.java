package com.minegolem.cGM.listener;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class InfinityArrowListener implements Listener {

    private final JavaPlugin plugin;

    public InfinityArrowListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBowShoot(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (event.isCancelled()) return;

        ItemStack weapon = event.getBow();
        if (weapon == null) return;

        ItemMeta meta = weapon.getItemMeta();
        if (meta == null || !meta.hasEnchant(Enchantment.INFINITY)) return;

        if (weapon.getType() != Material.BOW && weapon.getType() != Material.CROSSBOW) return;

        ItemStack consumedItem = event.getConsumable();
        if (consumedItem == null || !isSpecialArrowType(consumedItem.getType())) return;

        if (event.getProjectile() instanceof AbstractArrow arrow) {
            arrow.setPickupStatus(AbstractArrow.PickupStatus.CREATIVE_ONLY);
        }

        ItemStack arrowToRestore = consumedItem.clone();
        PlayerInventory inventory = player.getInventory();

        new BukkitRunnable() {
            @Override
            public void run() {
                if (inventory.firstEmpty() != -1) {
                    inventory.addItem(arrowToRestore);
                } else {
                    for (int i = 0; i < inventory.getSize(); i++) {
                        ItemStack item = inventory.getItem(i);
                        if (item != null && item.getType() == arrowToRestore.getType() &&
                                item.getAmount() < item.getMaxStackSize()) {
                            item.setAmount(item.getAmount() + 1);
                            break;
                        }
                    }
                }
                player.updateInventory();
            }
        }.runTaskLater(plugin, 1L);
    }

    private boolean isSpecialArrowType(Material material) {
        return material == Material.SPECTRAL_ARROW ||
                material == Material.TIPPED_ARROW;
    }
}
