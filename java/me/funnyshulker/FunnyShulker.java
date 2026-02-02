package me.funnyshulker;

import me.funnyshulker.listener.*;
import me.funnyshulker.manager.AutoCollectManager;
import me.funnyshulker.manager.ConfigManager;
import me.funnyshulker.manager.ShulkerManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class FunnyShulker extends JavaPlugin {

    private static FunnyShulker instance;
    private ConfigManager config;
    private ShulkerManager shulker;
    private AutoCollectManager autoCollect;

    @Override
    public void onEnable() {
        instance = this;

        this.config = new ConfigManager(this);
        this.shulker = new ShulkerManager(this);
        this.autoCollect = new AutoCollectManager(this);

        config.loadConfig();

        getServer().getPluginManager().registerEvents(new PlayerInteractListener(), this);
        getServer().getPluginManager().registerEvents(new InventoryClickListener(), this);
        getServer().getPluginManager().registerEvents(new QuickMoveListener(), this);

        autoCollect.start();
        getLogger().info("FunnyShulker загружен!");
    }

    @Override
    public void onDisable() {
        shulker.cleanup();
        autoCollect.stop();
        getLogger().info("FunnyShulker выключен!");
    }

    public static FunnyShulker getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return config;
    }

    public ShulkerManager getShulkerManager() {
        return shulker;
    }
}