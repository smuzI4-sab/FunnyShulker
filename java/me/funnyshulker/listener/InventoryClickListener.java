package me.funnyshulker.listener;

import me.funnyshulker.FunnyShulker;
import me.funnyshulker.inventory.ShulkerInventoryHolder;
import me.funnyshulker.manager.ConfigManager;
import me.funnyshulker.manager.ShulkerManager;
import me.funnyshulker.util.ItemUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class InventoryClickListener implements Listener {

    private final FunnyShulker plugin;
    private final ShulkerManager shulkerManager;
    private final ConfigManager configManager;

    public InventoryClickListener() {
        this.plugin = FunnyShulker.getInstance();
        this.shulkerManager = plugin.getShulkerManager();
        this.configManager = plugin.getConfigManager();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inventory = event.getInventory();
        Player player = (Player) event.getWhoClicked();

        if (!(inventory.getHolder() instanceof ShulkerInventoryHolder)) {
            checkPlayerInventory(event, player);
            return;
        }

        ShulkerInventoryHolder holder = (ShulkerInventoryHolder) inventory.getHolder();

        if (event.getCurrentItem() != null && configManager.isItemBlacklisted(event.getCurrentItem())) {
            event.setCancelled(true);
            player.sendMessage(configManager.getMessageManager().getMessage("shulker-blacklisted"));
            return;
        }

        ItemStack clickedItem = event.getCurrentItem();
        ItemStack cursorItem = event.getCursor();

        if (clickedItem != null && ItemUtil.isShulkerBox(clickedItem)) {
            event.setCancelled(true);
            player.sendMessage(configManager.getMessageManager().getMessage("shulker-self-remove"));
            return;
        }

        if (cursorItem != null && ItemUtil.isShulkerBox(cursorItem)) {
            event.setCancelled(true);
            player.sendMessage(configManager.getMessageManager().getMessage("shulker-self-remove"));
            return;
        }

        if (isSameShulker(holder, clickedItem) || isSameShulker(holder, cursorItem)) {
            event.setCancelled(true);
            player.sendMessage(configManager.getMessageManager().getMessage("shulker-self-remove"));
            return;
        }

        if (!event.isShiftClick()) {
            scheduleSave(player, inventory);
        }
    }

    private boolean isSameShulker(ShulkerInventoryHolder holder, ItemStack item) {
        if (item == null) return false;
        ItemStack original = holder.getShulkerData().getShulkerItem();
        return ItemUtil.isShulkerBox(item) && item.isSimilar(original);
    }

    private void checkPlayerInventory(InventoryClickEvent event, Player player) {
        if (!shulkerManager.hasOpenShulker(player.getUniqueId())) return;

        ItemStack current = event.getCurrentItem();
        ItemStack cursor = event.getCursor();
        ShulkerInventoryHolder holder = shulkerManager.getOpenInventory(player.getUniqueId());

        if (holder == null) return;

        boolean isShulker = (current != null && isSameShulker(holder, current)) ||
                (cursor != null && isSameShulker(holder, cursor));

        if (isShulker) {
            event.setCancelled(true);
            player.sendMessage(configManager.getMessageManager().getMessage("shulker-move-blocked"));
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Inventory inventory = event.getInventory();
        if (!(inventory.getHolder() instanceof ShulkerInventoryHolder)) return;
        shulkerManager.closeInventory((Player) event.getPlayer(), inventory);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        Inventory inventory = event.getInventory();
        if (!(inventory.getHolder() instanceof ShulkerInventoryHolder)) return;

        ShulkerInventoryHolder holder = (ShulkerInventoryHolder) inventory.getHolder();
        Player player = (Player) event.getWhoClicked();

        for (ItemStack item : event.getNewItems().values()) {
            if (configManager.isItemBlacklisted(item)) {
                event.setCancelled(true);
                player.sendMessage(configManager.getMessageManager().getMessage("shulker-blacklisted"));
                return;
            }

            if (ItemUtil.isShulkerBox(item) && isSameShulker(holder, item)) {
                event.setCancelled(true);
                player.sendMessage(configManager.getMessageManager().getMessage("shulker-self-remove"));
                return;
            }
        }

        scheduleSave(player, inventory);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (!shulkerManager.hasOpenShulker(player.getUniqueId())) return;

        ItemStack dropped = event.getItemDrop().getItemStack();
        ShulkerInventoryHolder holder = shulkerManager.getOpenInventory(player.getUniqueId());
        if (holder == null) return;

        if (isSameShulker(holder, dropped)) {
            event.setCancelled(true);
            player.sendMessage(configManager.getMessageManager().getMessage("shulker-drop-blocked"));
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        if (!shulkerManager.hasOpenShulker(player.getUniqueId())) return;

        ShulkerInventoryHolder holder = shulkerManager.getOpenInventory(player.getUniqueId());
        if (holder == null) return;

        ItemStack main = event.getMainHandItem();
        ItemStack off = event.getOffHandItem();

        boolean isShulker = (main != null && isSameShulker(holder, main)) ||
                (off != null && isSameShulker(holder, off));

        if (isShulker) {
            event.setCancelled(true);
            player.sendMessage(configManager.getMessageManager().getMessage("shulker-swap-blocked"));
        }
    }

    private void scheduleSave(Player player, Inventory inventory) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (player.isOnline() && inventory.getHolder() instanceof ShulkerInventoryHolder) {
                    shulkerManager.saveContents(player, inventory);
                }
            }
        }.runTask(plugin);
    }
}