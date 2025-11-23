package com.minegolem.cGM.food;

import com.minegolem.cGM.CGM;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.HashMap;
import java.util.Map;

public class FoodEffectListener implements Listener {

    private final CGM plugin;
    private final Map<Material, FoodEffect> effectsMap = new HashMap<>();

    public FoodEffectListener(CGM plugin, FoodEffect... effects) {
        this.plugin = plugin;
        for (FoodEffect effect : effects) {
            effectsMap.put(effect.getFoodMaterial(), effect);
        }
    }

    @EventHandler
    public void onItemConsume(PlayerItemConsumeEvent event) {
        FoodEffect effect = effectsMap.get(event.getItem().getType());
        if (effect != null) {
            effect.applyEffect(event.getPlayer(), plugin);
        }
    }

    @EventHandler
    public void onBlockInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block block = event.getClickedBlock();
        if (block == null) return;

        FoodEffect effect = effectsMap.get(block.getType());
        if (effect == null) return;

        Player player = event.getPlayer();
        if (player.getFoodLevel() < 20) {
            effect.applyEffect(player, plugin);
        }
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT) {
            event.setCancelled(true);
        }
    }
}
