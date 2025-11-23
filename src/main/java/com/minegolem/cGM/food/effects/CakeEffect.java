package com.minegolem.cGM.food.effects;

import com.minegolem.cGM.CGM;
import com.minegolem.cGM.food.FoodEffect;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class CakeEffect implements FoodEffect {

    @Override
    public Material getFoodMaterial() {
        return Material.CAKE;
    }

    @Override
    public void applyEffect(Player player, CGM plugin) {
        PotionEffect heroEffect = new PotionEffect(PotionEffectType.HERO_OF_THE_VILLAGE, 6000, 0, false, false, false);
        player.addPotionEffect(heroEffect);
    }
}
