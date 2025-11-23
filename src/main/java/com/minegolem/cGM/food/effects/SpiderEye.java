package com.minegolem.cGM.food.effects;

import com.minegolem.cGM.CGM;
import com.minegolem.cGM.food.FoodEffect;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class SpiderEye implements FoodEffect {

    @Override
    public Material getFoodMaterial() {
        return Material.SPIDER_EYE;
    }

    @Override
    public void applyEffect(Player player, CGM plugin) {
        PotionEffect effect = new PotionEffect(PotionEffectType.NIGHT_VISION, 6000, 0, false, false, false);
        player.addPotionEffect(effect);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            player.removePotionEffect(PotionEffectType.POISON);
        }, 1L);
    }
}
