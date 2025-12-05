package com.minegolem.cGM;

import com.minegolem.cGM.food.FoodEffectListener;
import com.minegolem.cGM.food.effects.*;
import com.minegolem.cGM.listener.*;
import com.minegolem.cGM.listener.logger.EffectLogger;
import org.bukkit.plugin.java.JavaPlugin;

public final class CGM extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        getServer().getPluginManager().registerEvents(new FoodEffectListener(this,
                new CakeEffect(),
                new PufferfishEffect(),
                new BeetrootSoup(),
                new CookedCod(),
                new CookedSalmon(),
                new Cookie(),
                new GlowBerry(),
                new Honey(),
                new Melon(),
                new MushroomStew(),
                new PoisonousPotato(),
                new RabbitStew(),
                new SpiderEye(),
                new SweetBerry(),
                new TropicalFish(),
                new PumpkinPie(),
                new ChorusFruit()
        ), this);

        getServer().getPluginManager().registerEvents(new TotemListener(this), this);
        getServer().getPluginManager().registerEvents(new DragonEggListener(this), this);
        getServer().getPluginManager().registerEvents(new TurtleHelmetListener(this), this);
        getServer().getPluginManager().registerEvents(new SnowballFreezeListener(), this);
        getServer().getPluginManager().registerEvents(new AnvilListener(), this);
        getServer().getPluginManager().registerEvents(new CrossbowEnchantmentListener(), this);
        getServer().getPluginManager().registerEvents(new InfinityArrowListener(this), this);
        getServer().getPluginManager().registerEvents(new SpawnerSilkTouchListener(), this);
        getServer().getPluginManager().registerEvents(new PotionStackListener(this), this);
       // getServer().getPluginManager().registerEvents(new EffectLogger(this), this);

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
