package com.minegolem.cGM.food.effects;

import com.minegolem.cGM.CGM;
import com.minegolem.cGM.food.FoodEffect;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PoisonousPotato implements FoodEffect {
    private final Random rand = new Random();
    private final List<SuspiciousEffect> possibleEffects = new ArrayList<>();

    public PoisonousPotato() {
        this.possibleEffects.add(new SuspiciousEffect(PotionEffectType.SATURATION, 6000, 0));
        this.possibleEffects.add(new SuspiciousEffect(PotionEffectType.BLINDNESS, 6000, 0));
        this.possibleEffects.add(new SuspiciousEffect(PotionEffectType.NIGHT_VISION, 6000, 0));
        this.possibleEffects.add(new SuspiciousEffect(PotionEffectType.POISON, 6000, 0));
        this.possibleEffects.add(new SuspiciousEffect(PotionEffectType.WEAKNESS, 6000, 0));
        this.possibleEffects.add(new SuspiciousEffect(PotionEffectType.JUMP_BOOST, 6000, 0));
        this.possibleEffects.add(new SuspiciousEffect(PotionEffectType.FIRE_RESISTANCE, 6000, 0));
        this.possibleEffects.add(new SuspiciousEffect(PotionEffectType.REGENERATION, 6000, 0));
        this.possibleEffects.add(new SuspiciousEffect(PotionEffectType.WITHER, 6000, 0));
    }

    @Override
    public Material getFoodMaterial() {
        return Material.POISONOUS_POTATO;
    }

    @Override
    public void applyEffect(Player player, CGM plugin) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            player.removePotionEffect(PotionEffectType.POISON);

            SuspiciousEffect randomEffect = (SuspiciousEffect) this.possibleEffects.get(this.rand.nextInt(this.possibleEffects.size()));
            PotionEffect effect = new PotionEffect(randomEffect.type(), randomEffect.duration(), randomEffect.amplifier(), false, false, false);

            player.addPotionEffect(effect);
        }, 1L);
    }

    private record SuspiciousEffect(PotionEffectType type, int duration, int amplifier) { }
}
