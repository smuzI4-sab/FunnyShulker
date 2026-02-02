package me.funnyshulker.listener;

import me.funnyshulker.FunnyShulker;
import me.funnyshulker.inventory.ShulkerInventoryHolder;
import me.funnyshulker.manager.ConfigManager;
import me.funnyshulker.manager.ShulkerManager;
import me.funnyshulker.util.ItemUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class QuickMoveListener implements Listener {

    private final FunnyShulker plugin;
    private final ShulkerManager shulkerManager;
    private final ConfigManager configManager;

    public QuickMoveListener() {
        this.plugin = FunnyShulker.getInstance();
        this.shulkerManager = plugin.getShulkerManager();
        this.configManager = plugin.getConfigManager();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onQuickMove(InventoryClickEvent event) {
        if (event.isCancelled()) return;

        Inventory top = event.getView().getTopInventory();
        if (!(top.getHolder() instanceof ShulkerInventoryHolder)) return;

        Player player = (Player) event.getWhoClicked();
        ShulkerInventoryHolder holder = (ShulkerInventoryHolder) top.getHolder();

        ItemStack clicked = event.getCurrentItem();
        if (clicked != null && ItemUtil.isShulkerBox(clicked)) {
            event.setCancelled(true);
            player.sendMessage(configManager.getMessageManager().getMessage("shulker-self-remove"));
            return;
        }

        if (event.isShiftClick()) {
            event.setCancelled(true);
            handleShiftClick(event, player, holder);
            return;
        }

        scheduleSave(player, holder.getInventory());
    }

    private void handleShiftClick(InventoryClickEvent event, Player player, ShulkerInventoryHolder holder) {
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        if (ItemUtil.isShulkerBox(clicked)) {
            player.sendMessage(configManager.getMessageManager().getMessage("shulker-self-remove"));
            return;
        }

        if (configManager.isItemBlacklisted(clicked)) {
            player.sendMessage(configManager.getMessageManager().getMessage("shulker-blacklisted"));
            return;
        }

        Inventory clickedInv = event.getClickedInventory();
        Inventory shulkerInv = holder.getInventory();

        if (clickedInv == player.getInventory()) {
            transferToShulker(player, shulkerInv, clicked, event.getSlot());
        } else if (clickedInv == shulkerInv) {
            transferToPlayer(player, shulkerInv, clicked, event.getSlot());
        }
    }

    private void transferToShulker(Player player, Inventory shulkerInv, ItemStack item, int slot) {
        new BukkitRunnable() {
            @Override
            public void run() {
                ItemStack toTransfer = item.clone();
                boolean transferred = false;

                for (int i = 0; i < shulkerInv.getSize(); i++) {
                    ItemStack shulkerSlot = shulkerInv.getItem(i);

                    if (shulkerSlot == null || shulkerSlot.getType() == Material.AIR) {
                        shulkerInv.setItem(i, toTransfer);
                        player.getInventory().setItem(slot, null);
                        transferred = true;
                        break;
                    } else if (shulkerSlot.isSimilar(toTransfer) &&
                            shulkerSlot.getAmount() < shulkerSlot.getMaxStackSize()) {

                        int transfer = Math.min(toTransfer.getAmount(),
                                shulkerSlot.getMaxStackSize() - shulkerSlot.getAmount());

                        shulkerSlot.setAmount(shulkerSlot.getAmount() + transfer);
                        shulkerInv.setItem(i, shulkerSlot);

                        if (toTransfer.getAmount() <= transfer) {
                            player.getInventory().setItem(slot, null);
                        } else {
                            toTransfer.setAmount(toTransfer.getAmount() - transfer);
                            player.getInventory().setItem(slot, toTransfer);
                        }
                        transferred = true;
                        break;
                    }
                }

                if (transferred) {
                    scheduleSave(player, shulkerInv);
                    configManager.getSoundManager().playPickupSound(player);
                } else {
                    player.sendMessage(configManager.getMessageManager().getMessage("shulker-no-space"));
                }

                player.updateInventory();
            }
        }.runTask(plugin);
    }

    private void transferToPlayer(Player player, Inventory shulkerInv, ItemStack item, int slot) {
        new BukkitRunnable() {
            @Override
            public void run() {
                ItemStack toTransfer = item.clone();
                boolean transferred = false;

                for (int i = 0; i < 36; i++) {
                    ItemStack playerSlot = player.getInventory().getItem(i);

                    if (playerSlot == null || playerSlot.getType() == Material.AIR) {
                        player.getInventory().setItem(i, toTransfer);
                        shulkerInv.setItem(slot, null);
                        transferred = true;
                        break;
                    } else if (playerSlot.isSimilar(toTransfer) &&
                            playerSlot.getAmount() < playerSlot.getMaxStackSize()) {

                        int transfer = Math.min(toTransfer.getAmount(),
                                playerSlot.getMaxStackSize() - playerSlot.getAmount());

                        playerSlot.setAmount(playerSlot.getAmount() + transfer);
                        player.getInventory().setItem(i, playerSlot);

                        if (toTransfer.getAmount() <= transfer) {
                            shulkerInv.setItem(slot, null);
                        } else {
                            toTransfer.setAmount(toTransfer.getAmount() - transfer);
                            shulkerInv.setItem(slot, toTransfer);
                        }
                        transferred = true;
                        break;
                    }
                }

                if (transferred) {
                    scheduleSave(player, shulkerInv);
                    configManager.getSoundManager().playPickupSound(player);
                }

                player.updateInventory();
            }
        }.runTask(plugin);
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