package com.minegolem.cGM.listener.logger;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class EffectLogger implements Listener {
    private final JavaPlugin plugin;
    public EffectLogger(JavaPlugin plugin) { this.plugin = plugin; }

    @EventHandler
    public void onPotionChange(EntityPotionEffectEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        Player p = (Player) e.getEntity();
        plugin.getLogger().info("[EffectLogger] " + p.getName() + " " + e.getAction() + " " +
                e.getModifiedType() + " old: " + e.getOldEffect() + " new: " + e.getNewEffect());

        if (e.getAction() == EntityPotionEffectEvent.Action.REMOVED) {
            Exception ex = new Exception();
            ex.printStackTrace();
        }
    }

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        plugin.getLogger().info("[EffectLogger] " + event.getPlayer().getName() + " consumed " + event.getItem().getType());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPotionRemoveLowest(EntityPotionEffectEvent e) { logEvent(e, "LOWEST"); }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPotionRemoveHigh(EntityPotionEffectEvent e) { logEvent(e, "HIGH"); }

    private void logEvent(EntityPotionEffectEvent e, String priority) {
        if (!(e.getEntity() instanceof Player)) return;
        Player p = (Player) e.getEntity();
        if (e.getAction() == EntityPotionEffectEvent.Action.REMOVED) {
            plugin.getLogger().info("[EffectLogger-" + priority + "] " + p.getName() + " REMOVED " + e.getModifiedType() + " old: " + e.getOldEffect());
        }
    }
}
