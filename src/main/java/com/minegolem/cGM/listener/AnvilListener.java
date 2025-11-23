package com.minegolem.cGM.listener;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Map;

public class AnvilListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onAnvilPrepare(PrepareAnvilEvent event) {
        AnvilInventory inventory = event.getInventory();
        ItemStack baseItem = inventory.getItem(0);  // Slot 1: Balestra
        ItemStack addition = inventory.getItem(1);  // Slot 2: Libro / Arco

        // Controllo oggetti validi
        if (baseItem == null || addition == null) return;
        if (baseItem.getType() != Material.CROSSBOW) return; // Solo balestra

        ItemStack result = baseItem.clone();
        ItemMeta resultMeta = result.getItemMeta();
        if (resultMeta == null) return;

        boolean hasEnchantments = false;
        int totalCost = 1;
        if (addition.getType() == Material.ENCHANTED_BOOK && addition.getItemMeta() instanceof EnchantmentStorageMeta bookMeta) {
            for (Map.Entry<Enchantment, Integer> entry : bookMeta.getStoredEnchants().entrySet()) {
                applyEnchant(resultMeta, entry.getKey(), entry.getValue());
                hasEnchantments = true;
                totalCost += entry.getValue();
            }
        }

        else if (addition.getType() == Material.BOW && addition.getItemMeta() != null) {
            for (Map.Entry<Enchantment, Integer> entry : addition.getItemMeta().getEnchants().entrySet()) {
                applyEnchant(resultMeta, entry.getKey(), entry.getValue());
                hasEnchantments = true;
                totalCost += entry.getValue();
            }
        }

        if (hasEnchantments) {
            result.setItemMeta(resultMeta);
            event.setResult(result);
            inventory.setRepairCost(Math.min(totalCost, 39));
        }
    }

    private void applyEnchant(ItemMeta meta, Enchantment enchantment, int level) {
        if (isBowEnchantment(enchantment)) {
            meta.addEnchant(enchantment, level, true);
        }
    }

    private boolean isBowEnchantment(Enchantment enchantment) {
        return enchantment == Enchantment.POWER ||
                enchantment == Enchantment.PUNCH ||
                enchantment == Enchantment.FLAME ||
                enchantment == Enchantment.INFINITY ||
                enchantment == Enchantment.UNBREAKING ||
                enchantment == Enchantment.MENDING;
    }
}
