package com.minegolem.cGM.listener;

import com.minegolem.cGM.CGM;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class TurtleHelmetListener implements Listener {
    private final CGM plugin;

    public TurtleHelmetListener(CGM plugin) {
        this.plugin = plugin;
        startHelmetCheckTask();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        checkTurtleHelmet(event.getPlayer());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        new BukkitRunnable() {
            @Override
            public void run() {
                checkTurtleHelmet(player);
            }
        }.runTaskLater(plugin, 1L);
    }

    private void startHelmetCheckTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    checkTurtleHelmet(player);
                }
            }
        }.runTaskTimer(plugin, 20L, 40L);
    }

    private void checkTurtleHelmet(Player player) {
        ItemStack helmet = player.getInventory().getHelmet();

        if (helmet != null && helmet.getType() == Material.TURTLE_HELMET) {
            if (!player.hasPotionEffect(PotionEffectType.DOLPHINS_GRACE)) {
                player.addPotionEffect(new PotionEffect(
                        PotionEffectType.DOLPHINS_GRACE,
                        Integer.MAX_VALUE,
                        0,
                        false,
                        false,
                        true
                ));
            }
        } else {
            player.removePotionEffect(PotionEffectType.DOLPHINS_GRACE);
           // player.removePotionEffect(PotionEffectType.WATER_BREATHING);
        }
    }
}
