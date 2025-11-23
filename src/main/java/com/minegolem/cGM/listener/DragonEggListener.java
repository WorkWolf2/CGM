package com.minegolem.cGM.listener;

import com.minegolem.cGM.CGM;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class DragonEggListener implements Listener {
    private final CGM plugin;

    public DragonEggListener(CGM plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntityType() != EntityType.ENDER_DRAGON) return;

        World world = event.getEntity().getWorld();
        if (world.getEnvironment() != World.Environment.THE_END) return;

        Bukkit.getScheduler().runTaskLater(plugin, () -> placeDragonEgg(world), 200L);
    }

    private void placeDragonEgg(World world) {
        Location baseLocation = new Location(world, 0, 0, 0);
        int highestY = world.getHighestBlockYAt(0, 0);

        Location checkLoc = baseLocation.clone();
        checkLoc.setY(highestY);
        Block highestBlock = world.getBlockAt(checkLoc);

        if (highestBlock.getType() == Material.BEDROCK) {
            Location eggLoc = checkLoc.clone().add(0, 1, 0);
            world.getBlockAt(eggLoc).setType(Material.DRAGON_EGG);

            plugin.getLogger().info("Uovo di drago piazzato correttamente in " + world.getName());
            return;
        }

        Location lowerLoc = baseLocation.clone();
        lowerLoc.setY(highestY - 1);

        Block secondBlock = world.getBlockAt(lowerLoc);

        if (secondBlock.getType() == Material.BEDROCK) {
            highestBlock.setType(Material.DRAGON_EGG);

            plugin.getLogger().warning("Uovo piazzato sopra blocchi non standard in " + world.getName());
            return;
        }

        plugin.getLogger().warning("⚠ Impossibile respawnare l'uovo in " + world.getName()
                + " (nessuna bedrock trovata)");
    }
}
