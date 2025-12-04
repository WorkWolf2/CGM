package com.minegolem.cGM.listener;

import com.minegolem.cGM.CGM;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class TotemListener implements Listener {
    private final CGM plugin;
    private final Set<UUID> invulnerablePlayers = new HashSet<>();

    public TotemListener(CGM plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        if (event.isCancelled()) return;

        if (invulnerablePlayers.contains(player.getUniqueId())) {
            event.setCancelled(true);
            return;
        }

        if (player.getHealth() - event.getFinalDamage() <= 0) {
            ItemStack mainHand = player.getInventory().getItemInMainHand();
            ItemStack offHand = player.getInventory().getItemInOffHand();

            if ((mainHand != null && mainHand.getType() == Material.TOTEM_OF_UNDYING)
                    || (offHand != null && offHand.getType() == Material.TOTEM_OF_UNDYING)) {

                makePlayerInvulnerable(player);
                return;
            }
            ItemStack totem = findTotemInInventory(player);
            if (totem != null) {
                event.setCancelled(true);
                totem.setAmount(totem.getAmount() - 1);
                player.setHealth(1.0);

                applyTotemEffects(player);
                makePlayerInvulnerable(player);
                playVanillaTotemAnimation(player);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityResurrect(EntityResurrectEvent event) {
        if (event.getEntity() instanceof Player player && !event.isCancelled()) {
            makePlayerInvulnerable(player);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location loc = player.getLocation();

        // Se sta cadendo nel vuoto nell'End
        if (loc.getY() < 0 && player.getWorld().getEnvironment() == World.Environment.THE_END) {
            ItemStack totem = findTotemInInventory(player);
            if (totem == null) return;

            totem.setAmount(totem.getAmount() - 1);

            Location safeLocation = findSafeLocationAdvanced(player);
            player.teleport(Objects.requireNonNullElseGet(safeLocation, () -> player.getWorld().getSpawnLocation()));

            applyTotemEffects(player);
            makePlayerInvulnerable(player);
            playCustomTotemAnimation(player);
        }
    }

    private void makePlayerInvulnerable(Player player) {
        final UUID playerId = player.getUniqueId();
        invulnerablePlayers.add(playerId);
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 100, 4, false, false, true));

        new BukkitRunnable() {
            @Override
            public void run() {
                invulnerablePlayers.remove(playerId);
            }
        }.runTaskLater(plugin, 100L);
    }

    private ItemStack findTotemInInventory(Player player) {
        PlayerInventory inv = player.getInventory();

        for (int slot = 0; slot < 9; slot++) { // Solo hotbar
            ItemStack item = inv.getItem(slot);

            if (item != null
                    && item.getType() == Material.TOTEM_OF_UNDYING
                    && item.getAmount() > 0) {
                return item;
            }
        }
        return null;
    }

    private void applyTotemEffects(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 900, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 100, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 800, 0));

        player.getActivePotionEffects().stream()
                .map(PotionEffect::getType)
                .filter(type -> type == PotionEffectType.POISON
                        || type == PotionEffectType.WITHER
                        || type == PotionEffectType.INSTANT_DAMAGE
                        || type == PotionEffectType.SLOWNESS
                        || type == PotionEffectType.MINING_FATIGUE
                        || type == PotionEffectType.WEAKNESS)
                .forEach(player::removePotionEffect);
    }

    private void playVanillaTotemAnimation(Player player) {
        player.playSound(player.getLocation(), Sound.ITEM_TOTEM_USE, 1.0F, 1.0F);
        player.playEffect(EntityEffect.TOTEM_RESURRECT);

        final ItemStack originalMain = player.getInventory().getItemInMainHand();
        final ItemStack originalOff = player.getInventory().getItemInOffHand();

        player.getInventory().setItemInMainHand(new ItemStack(Material.TOTEM_OF_UNDYING));
        player.updateInventory();

        new BukkitRunnable() {
            @Override
            public void run() {
                player.getInventory().setItemInMainHand(originalMain);
                player.getInventory().setItemInOffHand(originalOff);
                player.updateInventory();
            }
        }.runTaskLater(plugin, 2L);
    }

    private void playCustomTotemAnimation(Player player) {
        player.playSound(player.getLocation(), Sound.ITEM_TOTEM_USE, 1.0F, 1.0F);
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 0, false, false, true));

        // Timer puramente estetico
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (++ticks >= 20) cancel();
            }
        }.runTaskTimer(plugin, 0L, 1L);

        player.sendTitle("§6§l✦ TOTEM ATTIVATO ✦",
                "§eSei resuscitato!",
                10, 40, 10);
    }

    private Location findSafeLocationAdvanced(Player player) {
        World world = player.getWorld();
        Location origin = player.getLocation();

        Location[] checks = new Location[]{
                searchInRadius(world, origin, 25, 30),
                searchInRadius(world, origin, 50, 40),
                searchInRadius(world, origin, 100, 50),
                findEndIsland(world, origin),
                searchInRadius(world, origin, 200, 60)
        };

        for (Location loc : checks) {
            if (loc != null) return loc;
        }

        return null;
    }

    private Location searchInRadius(World world, Location center, int radius, int attempts) {
        for (int i = 0; i < attempts; i++) {
            int x = center.getBlockX() + (int) (Math.random() * (radius * 2) - radius);
            int z = center.getBlockZ() + (int) (Math.random() * (radius * 2) - radius);

            for (int y = Math.min(world.getMaxHeight() - 1, 255); y > 0; y--) {
                Location check = new Location(world, x, y, z);
                if (isSafeLocation(world, check)) {
                    return check.add(0.5, 1.0, 0.5);
                }
            }
        }
        return null;
    }

    private Location findEndIsland(World world, Location origin) {
        int[] distances = {500, 1000, 1500, 2000};

        for (int dist : distances) {
            for (int angle = 0; angle < 360; angle += 45) {
                double rad = Math.toRadians(angle);
                int x = (int) (origin.getX() + dist * Math.cos(rad));
                int z = (int) (origin.getZ() + dist * Math.sin(rad));

                for (int y = 80; y > 30; y--) {
                    Location check = new Location(world, x, y, z);
                    if (isSafeLocation(world, check)) {
                        return check.add(0.5, 1.0, 0.5);
                    }
                }
            }
        }
        return null;
    }


    private boolean isSafeLocation(World world, Location loc) {
        if (!world.getBlockAt(loc).getType().isSolid()) return false;

        Location above1 = loc.clone().add(0, 1, 0);
        Location above2 = loc.clone().add(0, 2, 0);

        if (!world.getBlockAt(above1).getType().isAir()
                || !world.getBlockAt(above2).getType().isAir()) {
            return false;
        }

        Material block = world.getBlockAt(loc).getType();
        return block != Material.LAVA
                && block != Material.FIRE
                && block != Material.MAGMA_BLOCK
                && block != Material.CACTUS;
    }
}
