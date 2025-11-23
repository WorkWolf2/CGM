package com.minegolem.cGM.food;

import com.minegolem.cGM.CGM;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public interface FoodEffect {
    Material getFoodMaterial();

    void applyEffect(Player player, CGM plugin);
}
