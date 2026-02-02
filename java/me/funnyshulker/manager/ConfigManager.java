package me.funnyshulker.manager;

import me.funnyshulker.FunnyShulker;
import me.funnyshulker.util.MessageManager;
import me.funnyshulker.util.SoundManager;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class ConfigManager {

    private final FunnyShulker plugin;
    private FileConfiguration config;
    private SoundManager soundManager;
    private MessageManager messageManager;
    private String shulkerNameFormat;
    private boolean smartPlacement;
    private String openMethod;
    private Set<Material> blacklistedItems;
    private boolean autoCollectOnlyWhenInventoryFull;
    private boolean autoCollectEnabled;
    private int autoCollectInterval;
    private double autoCollectMaxDistance;
    private boolean showAutoCollectMessages;
    private Set<Material> autoCollectBlacklist;
    private List<Material> autoCollectPriorityItems;

    public ConfigManager(FunnyShulker plugin) {
        this.plugin = plugin;
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        this.config = plugin.getConfig();

        this.soundManager = new SoundManager(config);
        this.messageManager = new MessageManager(config);

        this.shulkerNameFormat = config.getString("settings.shulker-name-format", "&f{shulker_name}");
        this.smartPlacement = config.getBoolean("settings.smart-placement", true);
        this.openMethod = config.getString("settings.open-method", "smart");

        this.blacklistedItems = loadMaterialSet("settings.blacklisted-items",
                Material.BEDROCK, Material.BARRIER, Material.COMMAND_BLOCK,
                Material.STRUCTURE_BLOCK, Material.JIGSAW, Material.SPAWNER);

        loadAutoCollectSettings();
    }

    private Set<Material> loadMaterialSet(String path, Material... defaults) {
        Set<Material> materials = new HashSet<> ();
        List<String> stringList = config.getStringList(path);

        if (stringList.isEmpty() && defaults.length > 0) {
            Collections.addAll(materials, defaults);
        } else {
            for (String itemName : stringList) {
                try {
                    materials.add(Material.valueOf(itemName.toUpperCase()));
                } catch (IllegalArgumentException ignored) {}
            }
        }
        return materials;
    }

    private void loadAutoCollectSettings() {
        ConfigurationSection section = config.getConfigurationSection("settings.auto-collect");
        if (section != null) {
            this.autoCollectEnabled = section.getBoolean("enabled", true);
            this.autoCollectInterval = section.getInt("check-interval", 20);
            this.autoCollectMaxDistance = section.getDouble("max-distance", 3.0);
            this.showAutoCollectMessages = section.getBoolean("collect-messages", false);
            this.autoCollectOnlyWhenInventoryFull = section.getBoolean("only-when-inventory-full", true);

            this.autoCollectBlacklist = loadMaterialSet("settings.auto-collect.blacklist",
                    Material.BEDROCK, Material.BARRIER, Material.SPAWNER,
                    Material.COMMAND_BLOCK, Material.STRUCTURE_BLOCK, Material.JIGSAW);

            List<String> priority = config.getStringList("settings.auto-collect.priority-items");
            this.autoCollectPriorityItems = new ArrayList<>();
            for (String item : priority) {
                try {
                    autoCollectPriorityItems.add(Material.valueOf(item.toUpperCase()));
                } catch (IllegalArgumentException ignored) {}
            }
        } else {
            loadDefaultAutoCollect();
        }
    }

    public boolean isAutoCollectOnlyWhenInventoryFull() {
        return autoCollectOnlyWhenInventoryFull;
    }

    private void loadDefaultAutoCollect() {
        this.autoCollectEnabled = true;
        this.autoCollectInterval = 20;
        this.autoCollectMaxDistance = 3.0;
        this.showAutoCollectMessages = false;

        this.autoCollectBlacklist = new HashSet<>(Arrays.asList(
                Material.BEDROCK, Material.BARRIER, Material.SPAWNER,
                Material.COMMAND_BLOCK, Material.STRUCTURE_BLOCK, Material.JIGSAW
        ));

        this.autoCollectPriorityItems = Arrays.asList(
                Material.DIAMOND, Material.NETHERITE_SCRAP, Material.NETHERITE_INGOT,
                Material.EMERALD, Material.GOLD_INGOT, Material.IRON_INGOT
        );
    }

    public SoundManager getSoundManager() {
        return soundManager;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

    public String getShulkerNameFormat() {
        return shulkerNameFormat;
    }

    public boolean isSmartPlacement() {
        return smartPlacement;
    }

    public String getOpenMethod() {
        return openMethod;
    }

    public boolean isItemBlacklisted(ItemStack item) {
        return item != null && blacklistedItems.contains(item.getType());
    }

    public boolean isAutoCollectEnabled() {
        return autoCollectEnabled;
    }

    public int getAutoCollectInterval() {
        return autoCollectInterval;
    }

    public double getAutoCollectMaxDistance() {
        return autoCollectMaxDistance;
    }

    public boolean showAutoCollectMessages() {
        return showAutoCollectMessages;
    }

    public Set<Material> getAutoCollectBlacklist() {
        return autoCollectBlacklist;
    }

    public List<Material> getAutoCollectPriorityItems() {
        return autoCollectPriorityItems;
    }

    public boolean isItemBlacklistedForAutoCollect(ItemStack item) {
        return item != null && autoCollectBlacklist.contains(item.getType());
    }

    public void reload() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();
        loadConfig();
    }
}