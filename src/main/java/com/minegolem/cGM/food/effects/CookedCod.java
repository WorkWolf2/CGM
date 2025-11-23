package com.minegolem.cGM.food.effects;

import com.minegolem.cGM.CGM;
import com.minegolem.cGM.food.FoodEffect;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class CookedCod implements FoodEffect {

    @Override
    public Material getFoodMaterial() {
        return Material.COOKED_COD;
    }

    @Override
    public void applyEffect(Player player, CGM plugin) {
        PotionEffect waterBreathing = new PotionEffect(PotionEffectType.WATER_BREATHING, 6000, 0, false, false, false);
        player.addPotionEffect(waterBreathing);
    }
}
