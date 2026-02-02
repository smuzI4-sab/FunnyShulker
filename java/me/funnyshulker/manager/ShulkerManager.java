package me.funnyshulker.manager;

import me.funnyshulker.FunnyShulker;
import me.funnyshulker.inventory.ShulkerInventoryHolder;
import me.funnyshulker.model.ShulkerData;
import me.funnyshulker.util.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class ShulkerManager {

    private final Map<UUID, ShulkerInventoryHolder> openInventories = new HashMap<>();
    private final Map<UUID, ItemStack> originalShulkerItems = new HashMap<>();
    private final Map<UUID, Long> lastSaveTime = new HashMap<>();
    private final Set<UUID> saveInProgress = new HashSet<>();

    private final FunnyShulker plugin;

    public ShulkerManager(FunnyShulker plugin) {
        this.plugin = plugin;
    }

    public boolean openShulker(Player player, ItemStack shulker, int slot) {
        if (!ItemUtil.isShulkerBox(shulker)) return false;

        UUID playerId = player.getUniqueId();
        if (openInventories.containsKey(playerId)) {
            ShulkerInventoryHolder holder = openInventories.get(playerId);
            if (holder != null && shulker.isSimilar(holder.getShulkerData().getShulkerItem())) {
                return false;
            }
        }

        try {
            ShulkerData data = new ShulkerData(playerId, shulker, slot);
            ShulkerInventoryHolder holder = new ShulkerInventoryHolder(data);

            loadContents(shulker, holder.getInventory());
            player.openInventory(holder.getInventory());

            openInventories.put(playerId, holder);
            originalShulkerItems.put(playerId, shulker.clone());

            plugin.getConfigManager().getSoundManager().playOpenSound(player);
            return true;

        } catch (Exception e) {
            plugin.getLogger().severe("Ошибка открытия шалкер-бокса: " + e.getMessage());
            return false;
        }
    }

    public void saveContents(Player player, Inventory inventory) {
        UUID playerId = player.getUniqueId();

        if (saveInProgress.contains(playerId)) return;
        if (lastSaveTime.containsKey(playerId) &&
                System.currentTimeMillis() - lastSaveTime.get(playerId) < 50) {
            return;
        }

        saveInProgress.add(playerId);

        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    ShulkerInventoryHolder holder = (ShulkerInventoryHolder) inventory.getHolder();
                    if (holder == null) return;

                    ShulkerData data = holder.getShulkerData();
                    ItemStack shulkerItem = originalShulkerItems.get(playerId);

                    if (shulkerItem == null) {
                        shulkerItem = data.getShulkerItem();
                    }

                    ItemStack finalItem = shulkerItem.clone();
                    saveContentsToItem(finalItem, inventory);

                    int slot = data.getSlot();
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            ItemStack current = player.getInventory().getItem(slot);
                            if (current != null && ItemUtil.isShulkerBox(current)) {
                                player.getInventory().setItem(slot, finalItem);
                                originalShulkerItems.put(playerId, finalItem.clone());
                                player.updateInventory();
                            }
                            lastSaveTime.put(playerId, System.currentTimeMillis());
                            saveInProgress.remove(playerId);
                        }
                    }.runTask(plugin);

                } catch (Exception e) {
                    saveInProgress.remove(playerId);
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    public void closeInventory(Player player, Inventory inventory) {
        saveContents(player, inventory);

        UUID playerId = player.getUniqueId();
        openInventories.remove(playerId);
        originalShulkerItems.remove(playerId);
        lastSaveTime.remove(playerId);
        saveInProgress.remove(playerId);

        plugin.getConfigManager().getSoundManager().playCloseSound(player);
    }

    private void loadContents(ItemStack shulker, Inventory inventory) {
        if (!(shulker.getItemMeta() instanceof BlockStateMeta)) return;

        BlockStateMeta meta = (BlockStateMeta) shulker.getItemMeta();
        if (meta.getBlockState() instanceof org.bukkit.block.ShulkerBox) {
            org.bukkit.block.ShulkerBox box = (org.bukkit.block.ShulkerBox) meta.getBlockState();
            ItemStack[] contents = box.getInventory().getContents();

            for (int i = 0; i < Math.min(contents.length, 27); i++) {
                inventory.setItem(i, contents[i]);
            }
        }
    }

    private void saveContentsToItem(ItemStack shulker, Inventory inventory) {
        if (!(shulker.getItemMeta() instanceof BlockStateMeta)) return;

        BlockStateMeta meta = (BlockStateMeta) shulker.getItemMeta();
        if (meta.getBlockState() instanceof org.bukkit.block.ShulkerBox) {
            org.bukkit.block.ShulkerBox box = (org.bukkit.block.ShulkerBox) meta.getBlockState();
            box.getInventory().clear();

            for (int i = 0; i < 27; i++) {
                ItemStack item = inventory.getItem(i);
                if (item != null && item.getType() != Material.AIR) {
                    box.getInventory().setItem(i, item.clone());
                }
            }

            meta.setBlockState(box);
            shulker.setItemMeta(meta);
        }
    }

    public ShulkerInventoryHolder getOpenInventory(UUID playerId) {
        return openInventories.get(playerId);
    }

    public boolean hasOpenShulker(UUID playerId) {
        return openInventories.containsKey(playerId);
    }

    public ItemStack getOriginalShulker(UUID playerId) {
        return originalShulkerItems.get(playerId);
    }

    public boolean isSameShulker(UUID playerId, ItemStack item) {
        if (item == null) return false;
        ItemStack original = getOriginalShulker(playerId);
        return original != null && item.isSimilar(original);
    }

    public void cleanup() {
        for (UUID playerId : openInventories.keySet()) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.getOpenInventory() != null) {
                player.closeInventory();
            }
        }
        openInventories.clear();
        originalShulkerItems.clear();
        lastSaveTime.clear();
        saveInProgress.clear();
    }
}