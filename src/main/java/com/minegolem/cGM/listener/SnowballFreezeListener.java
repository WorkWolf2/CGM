package com.minegolem.cGM.listener;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class SnowballFreezeListener implements Listener {
    @EventHandler
    public void onSnowballHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Snowball snowball)) return;
        if (!(snowball.getShooter() instanceof Player)) return;
        if (event.getHitEntity() != null && !(event.getHitEntity() instanceof LivingEntity)) return;

        LivingEntity hitEntity = (LivingEntity )event.getHitEntity();
        PotionEffect slowEffect = new PotionEffect(PotionEffectType.SLOWNESS, 60, 1, false, true, true);

        if (hitEntity != null) hitEntity.addPotionEffect(slowEffect);
    }
}
