package com.minegolem.cGM.manager;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PotionStackManager {

    private static final String STACK_LORE_PREFIX = ChatColor.GRAY + "Items: " + ChatColor.WHITE;

    public static boolean canStack(ItemStack a, ItemStack b) {
        if (a == null || b == null) return false;
        if (a.getAmount() <= 0 || b.getAmount() <= 0) return false;
        if (!isPotionType(a.getType()) || !isPotionType(b.getType())) return false;
        if (a.getType() != b.getType()) return false;

        if (!(a.getItemMeta() instanceof PotionMeta metaA) || !(b.getItemMeta() instanceof PotionMeta metaB)) {
            return false;
        }

        if (metaA.getBasePotionType() != metaB.getBasePotionType()) return false;

        if (metaA.hasCustomEffects() != metaB.hasCustomEffects()) return false;
        if (metaA.hasCustomEffects() && !metaA.getCustomEffects().equals(metaB.getCustomEffects())) return false;

        if (metaA.hasColor() != metaB.hasColor()) return false;
        if (metaA.hasColor() && !Objects.equals(metaA.getColor(), metaB.getColor())) return false;

        String nameA = metaA.hasDisplayName() ? metaA.getDisplayName() : null;
        String nameB = metaB.hasDisplayName() ? metaB.getDisplayName() : null;
        if (!Objects.equals(nameA, nameB)) return false;

        List<String> loreA = getOriginalLore(metaA);
        List<String> loreB = getOriginalLore(metaB);
        if (loreA == null && loreB != null) return false;

        return loreA == null || loreA.equals(loreB);
    }

    public static boolean isPotionType(Material material) {
        return material == Material.POTION ||
                material == Material.SPLASH_POTION ||
                material == Material.LINGERING_POTION;
    }

    public static ItemStack normalizePotion(ItemStack item) {
        if (item == null || !isPotionType(item.getType())) return item;
        if (!(item.getItemMeta() instanceof PotionMeta)) return item;
        return updateStackLore(item);
    }

    public static ItemStack updateStackLore(ItemStack item) {
        if (item == null || !isPotionType(item.getType())) return item;
        if (!(item.getItemMeta() instanceof PotionMeta meta)) return item;

        List<String> originalLore = getOriginalLore(meta);

        if (item.getAmount() <= 1) {
            if (originalLore != null) {
                meta.setLore(new ArrayList<>(originalLore));
            } else {
                meta.setLore(null);
            }
        } else {
            List<String> newLore = new ArrayList<>();
            if (originalLore != null) {
                newLore.addAll(originalLore);
            }
            newLore.add("");
            newLore.add(STACK_LORE_PREFIX + item.getAmount());
            meta.setLore(newLore);
        }

        item.setItemMeta(meta);
        return item;
    }

    private static List<String> getOriginalLore(PotionMeta meta) {
        if (!meta.hasLore()) return null;

        List<String> originalLore = meta.getLore();
        if (originalLore == null) return null;

        List<String> filteredLore = new ArrayList<>();
        for (String line : originalLore) {
            if (line.startsWith(STACK_LORE_PREFIX)) break;
            filteredLore.add(line);
        }

        while (!filteredLore.isEmpty() && filteredLore.get(filteredLore.size() - 1).isEmpty()) {
            filteredLore.remove(filteredLore.size() - 1);
        }

        return filteredLore.isEmpty() ? null : filteredLore;
    }

    public static void forceUpdateStackLore(ItemStack item) {
        if (item != null && isPotionType(item.getType())) {
            updateStackLore(item);
        }
    }

    public static int stackPotions(ItemStack target, ItemStack source, int maxStack) {
        if (!canStack(target, source)) return 0;
        if (target.getAmount() >= maxStack) return 0;

        int space = maxStack - target.getAmount();
        int amountToMove = Math.min(source.getAmount(), space);

        target.setAmount(target.getAmount() + amountToMove);
        source.setAmount(source.getAmount() - amountToMove);

        updateStackLore(target);
        if (source.getAmount() > 0) {
            updateStackLore(source);
        }

        return amountToMove;
    }
}
