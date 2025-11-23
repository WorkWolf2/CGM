package com.minegolem.cGM.food.effects;

import com.minegolem.cGM.CGM;
import com.minegolem.cGM.food.FoodEffect;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PufferfishEffect implements FoodEffect {

    @Override
    public Material getFoodMaterial() {
        return Material.PUFFERFISH;
    }

    @Override
    public void applyEffect(Player player, CGM plugin) {
        PotionEffect effect = new PotionEffect(PotionEffectType.CONDUIT_POWER, 200, 1);
        player.addPotionEffect(effect);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            player.removePotionEffect(PotionEffectType.POISON);
            player.removePotionEffect(PotionEffectType.NAUSEA);
            player.removePotionEffect(PotionEffectType.HUNGER);
        }, 1L);
    }
}
