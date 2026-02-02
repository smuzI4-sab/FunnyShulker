package me.funnyshulker.util;

import me.funnyshulker.FunnyShulker;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class SoundManager {

    private Sound openSound;
    private Sound closeSound;
    private Sound pickupSound;

    public SoundManager(FileConfiguration config) {
        loadSounds(config);
    }

    private void loadSounds(FileConfiguration config) {
        this.openSound = getSound(config, "sounds.open", "BLOCK_SHULKER_BOX_OPEN");
        this.closeSound = getSound(config, "sounds.close", "BLOCK_SHULKER_BOX_CLOSE");
        this.pickupSound = getSound(config, "sounds.pickup", "ENTITY_ITEM_PICKUP");

        FunnyShulker.getInstance().getLogger().info("Звуки загружены: open=" + openSound +
                ", close=" + closeSound + ", pickup=" + pickupSound);
    }

    private Sound getSound(FileConfiguration config, String path, String defaultValue) {
        String soundName = config.getString(path, defaultValue);
        try {
            Sound sound = Sound.valueOf(soundName);
            FunnyShulker.getInstance().getLogger().info("Звук " + path + " = " + sound);
            return sound;
        } catch (IllegalArgumentException e) {
            FunnyShulker.getInstance().getLogger().warning("Неизвестный звук: " + soundName + ", используем " + defaultValue);
            return Sound.valueOf(defaultValue);
        }
    }

    public void playOpenSound(Player player) {
        if (openSound != null && player != null) {
            player.playSound(player.getLocation(), openSound, 1.0f, 1.0f);
        }
    }

    public void playCloseSound(Player player) {
        if (closeSound != null && player != null) {
            player.playSound(player.getLocation(), closeSound, 1.0f, 1.0f);
        }
    }

    public void playPickupSound(Player player) {
        if (pickupSound != null && player != null) {
            player.playSound(player.getLocation(), pickupSound, 0.5f, 1.0f);
        }
    }

    public Sound getPickupSound() {
        return pickupSound;
    }
}