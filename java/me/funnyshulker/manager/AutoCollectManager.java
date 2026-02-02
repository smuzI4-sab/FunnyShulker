package me.funnyshulker.manager;

import me.funnyshulker.FunnyShulker;
import me.funnyshulker.util.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class AutoCollectManager {

    private final FunnyShulker plugin;
    private final ConfigManager config;
    private BukkitRunnable task;

    public AutoCollectManager(FunnyShulker plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfigManager();
    }

    public void start() {
        if (!config.isAutoCollectEnabled()) {
            plugin.getLogger().info("Автосбор отключен в конфиге");
            return;
        }

        if (task != null) task.cancel();

        task = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    processPlayer(player);
                }
            }
        };

        task.runTaskTimer(plugin, 20L, config.getAutoCollectInterval());
        plugin.getLogger().info("Автосбор запущен (интервал: " + config.getAutoCollectInterval() + ")");
    }

    private void processPlayer(Player player) {
        boolean full = isInventoryFull(player);

        if (config.isAutoCollectOnlyWhenInventoryFull() && !full) {
            return;
        }

        List<Item> nearbyItems = getNearbyItems(player);

        if (nearbyItems.isEmpty()) return;

        ItemStack shulker = findShulker(player);

        if (shulker == null) {
            if (config.showAutoCollectMessages()) {
                player.sendMessage(config.getMessageManager().getMessage("auto-collect-no-shulker"));
            }
            return;
        }

        int collected = 0;
        for (Item item : nearbyItems) {
            if (item.isDead() || !item.isValid()) continue;

            ItemStack itemStack = item.getItemStack();
            if (itemStack == null) continue;

            if (config.isItemBlacklistedForAutoCollect(itemStack)) continue;

            if (collectToShulker(player, shulker, itemStack.clone())) {
                item.remove();
                collected++;
                config.getSoundManager().playPickupSound(player);
            }
        }

        if (collected > 0 && config.showAutoCollectMessages()) {
            player.sendMessage("§7Собрано предметов: §f" + collected);
        }
    }

    private boolean isInventoryFull(Player player) {
        ItemStack[] contents = player.getInventory().getStorageContents();

        for (ItemStack item : contents) {
            if (item == null || item.getType() == Material.AIR) {
                return false;
            }
        }
        return true;
    }

    private List<Item> getNearbyItems(Player player) {
        List<Item> items = new ArrayList<>();
        double maxDist = config.getAutoCollectMaxDistance();

        for (Item item : player.getWorld().getEntitiesByClass(Item.class)) {
            if (item == null || item.isDead()) continue;
            if (item.getLocation().distance(player.getLocation()) <= maxDist) {
                items.add(item);
            }
        }
        return items;
    }

    private ItemStack findShulker(Player player) {
        for (Material priority : config.getAutoCollectPriorityItems()) {
            for (ItemStack item : player.getInventory().getContents()) {
                if (item != null && ItemUtil.isShulkerBox(item) &&
                        item.getType() == priority && hasSpace(item)) {
                    return item;
                }
            }
        }

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && ItemUtil.isShulkerBox(item) && hasSpace(item)) {
                return item;
            }
        }
        return null;
    }

    private boolean hasSpace(ItemStack shulker) {
        if (!(shulker.getItemMeta() instanceof BlockStateMeta)) return false;

        BlockStateMeta meta = (BlockStateMeta) shulker.getItemMeta();
        if (meta.getBlockState() instanceof org.bukkit.block.ShulkerBox) {
            org.bukkit.block.ShulkerBox box = (org.bukkit.block.ShulkerBox) meta.getBlockState();
            for (ItemStack item : box.getInventory().getContents()) {
                if (item == null || item.getType() == Material.AIR) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean collectToShulker(Player player, ItemStack shulker, ItemStack item) {
        if (!(shulker.getItemMeta() instanceof BlockStateMeta)) return false;

        BlockStateMeta meta = (BlockStateMeta) shulker.getItemMeta();
        if (!(meta.getBlockState() instanceof org.bukkit.block.ShulkerBox)) return false;

        org.bukkit.block.ShulkerBox box = (org.bukkit.block.ShulkerBox) meta.getBlockState();

        for (int i = 0; i < 27; i++) {
            ItemStack slot = box.getInventory().getItem(i);

            if (slot == null || slot.getType() == Material.AIR) {
                box.getInventory().setItem(i, item);
                meta.setBlockState(box);
                shulker.setItemMeta(meta);
                updatePlayerInventory(player, shulker);
                return true;
            } else if (slot.isSimilar(item) && slot.getAmount() < slot.getMaxStackSize()) {
                int transfer = Math.min(item.getAmount(), slot.getMaxStackSize() - slot.getAmount());
                slot.setAmount(slot.getAmount() + transfer);
                box.getInventory().setItem(i, slot);

                if (item.getAmount() > transfer) {
                    item.setAmount(item.getAmount() - transfer);
                    return collectToShulker(player, shulker, item);
                }

                meta.setBlockState(box);
                shulker.setItemMeta(meta);
                updatePlayerInventory(player, shulker);
                return true;
            }
        }

        if (config.showAutoCollectMessages()) {
            player.sendMessage(config.getMessageManager().getMessage("auto-collect-full"));
        }
        return false;
    }

    private void updatePlayerInventory(Player player, ItemStack updatedShulker) {
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item != null && ItemUtil.isShulkerBox(item) &&
                    item.getType() == updatedShulker.getType()) {
                player.getInventory().setItem(i, updatedShulker);
                player.updateInventory();
                return;
            }
        }
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }
}