package com.minegolem.cGM.food.effects;

import com.minegolem.cGM.CGM;
import com.minegolem.cGM.food.FoodEffect;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class TropicalFish implements FoodEffect {

    @Override
    public Material getFoodMaterial() {
        return Material.TROPICAL_FISH;
    }

    @Override
    public void applyEffect(Player player, CGM plugin) {
        PotionEffect effect = new PotionEffect(PotionEffectType.CONDUIT_POWER, 6000, 0, false, false, false);
        player.addPotionEffect(effect);
    }
}
