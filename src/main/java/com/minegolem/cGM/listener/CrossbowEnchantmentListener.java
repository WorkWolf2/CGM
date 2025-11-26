package com.minegolem.cGM.listener;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import static com.minegolem.cGM.utilities.WorldGuardUtils.canHit;
import static com.minegolem.cGM.utilities.WorldGuardUtils.isInOwnClaim;

public class CrossbowEnchantmentListener implements Listener {
    @EventHandler
    public void onCrossbowShoot(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        ItemStack bow = event.getBow();
        if (bow == null || bow.getType() != Material.CROSSBOW) return;

        ItemMeta meta = bow.getItemMeta();
        if (meta == null) return;

        if (meta.hasEnchant(Enchantment.FLAME) && event.getProjectile() instanceof Arrow arrow) {
            arrow.setFireTicks(Integer.MAX_VALUE);
        }

        if (meta.hasEnchant(Enchantment.POWER) && event.getProjectile() instanceof AbstractArrow arrow) {
            int level = meta.getEnchantLevel(Enchantment.POWER);
            arrow.setDamage(arrow.getDamage() + (level * 0.5) + 0.5);
        }

        if (meta.hasEnchant(Enchantment.INFINITY)) {
            event.setConsumeItem(false);
        }
    }

    @EventHandler
    public void onArrowHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Arrow arrow)) return;
        if (!(arrow.getShooter() instanceof Player player)) return;

        Location hitLocation;
        if (event.getHitEntity() != null) {
            hitLocation = event.getHitEntity().getLocation();
        } else if (event.getHitBlock() != null) {
            hitLocation = event.getHitBlock().getLocation();
        } else return;

        boolean isInsideOwnClaim = isInOwnClaim(player, hitLocation);
        boolean isGlobal = canHit(player, hitLocation);

        if (event.getHitEntity() instanceof Player) {
            if (!isGlobal) {
                arrow.remove();
                event.setCancelled(true);
                player.sendMessage("§cNon puoi colpire altri giocatori nelle zone protette!");
            }
            return;
        }

        if (event.getHitEntity() instanceof LivingEntity entity) {
            boolean isHostile = (entity instanceof Monster || entity instanceof Phantom || entity instanceof Shulker);

            if (!isHostile && !isGlobal) {
                arrow.remove();
                event.setCancelled(true);
                player.sendMessage("§cPuoi colpire solo i mob ostili in claim altrui!");
                return;
            }

            applyFlameIfNeeded(player, event);
            return;
        }

        if (!isGlobal) {
            arrow.remove();
            event.setCancelled(true);
            player.sendMessage("§cNon puoi lanciare frecce in questa zona!");
        }
    }

    private ItemStack getCrossbowInHands(Player player) {
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (mainHand.getType() == Material.CROSSBOW) return mainHand;

        ItemStack offHand = player.getInventory().getItemInOffHand();
        if (offHand.getType() == Material.CROSSBOW) return offHand;

        return null;
    }

    private void applyFlameIfNeeded(Player player, ProjectileHitEvent event) {
        ItemStack crossbow = getCrossbowInHands(player);
        if (crossbow == null || crossbow.getItemMeta() == null) return;

        ItemMeta meta = crossbow.getItemMeta();
        if (!meta.hasEnchant(Enchantment.FLAME)) return;

        if (event.getHitEntity() != null) {
            event.getHitEntity().setFireTicks(100);
        }
    }
}
