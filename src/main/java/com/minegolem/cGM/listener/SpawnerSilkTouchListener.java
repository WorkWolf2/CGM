package com.minegolem.cGM.listener;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

public class SpawnerSilkTouchListener implements Listener {

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) return;
        if (event.getBlock().getType() != Material.SPAWNER) return;

        Player player = event.getPlayer();
        ItemStack tool = player.getInventory().getItemInMainHand();

        if (!isPickaxe(tool.getType())) return;
        if (!tool.containsEnchantment(Enchantment.SILK_TOUCH)) return;

        event.setDropItems(false);
        event.setExpToDrop(0);

        ItemStack emptySpawner = createEmptySpawner();

        player.getWorld().dropItemNaturally(event.getBlock().getLocation().clone().add(.5, 0, .5), emptySpawner);
    }

    private boolean isPickaxe(Material material) {
        return switch (material) {
            case WOODEN_PICKAXE, STONE_PICKAXE, IRON_PICKAXE,
                 GOLDEN_PICKAXE, DIAMOND_PICKAXE, NETHERITE_PICKAXE -> true;
            default -> false;
        };
    }

    private ItemStack createEmptySpawner() {
        ItemStack spawner = new ItemStack(Material.SPAWNER);
        BlockStateMeta meta = (BlockStateMeta) spawner.getItemMeta();

        if (meta != null) {
            CreatureSpawner spawnerState = (CreatureSpawner) meta.getBlockState();
            spawnerState.setSpawnedType(null);
            meta.setBlockState(spawnerState);

            meta.setDisplayName(ChatColor.GRAY + "Spawner");
            spawner.setItemMeta(meta);
        }

        return spawner;
    }
}
