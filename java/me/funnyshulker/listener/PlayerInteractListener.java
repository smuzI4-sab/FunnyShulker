package me.funnyshulker.listener;

import me.funnyshulker.FunnyShulker;
import me.funnyshulker.manager.ConfigManager;
import me.funnyshulker.manager.ShulkerManager;
import me.funnyshulker.util.ItemUtil;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class PlayerInteractListener implements Listener {

    public final FunnyShulker plugin;
    private final ShulkerManager shulkerManager;
    private final ConfigManager configManager;

    public PlayerInteractListener() {
        this.plugin = FunnyShulker.getInstance();
        this.shulkerManager = plugin.getShulkerManager();
        this.configManager = plugin.getConfigManager();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (event.getAction() != Action.RIGHT_CLICK_AIR &&
                event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        ItemStack item = player.getInventory().getItemInMainHand();

        if (!ItemUtil.isShulkerBox(item)) {
            return;
        }

        if (configManager.isItemBlacklisted(item)) {
            player.sendMessage(configManager.getMessageManager().getMessage("shulker-blacklisted"));
            event.setCancelled(true);
            return;
        }

        if (shulkerManager.hasOpenShulker(player.getUniqueId())) {
            if (shulkerManager.isSameShulker(player.getUniqueId(), item)) {
                event.setCancelled(true);
                player.sendMessage(configManager.getMessageManager().getMessage("shulker-move-blocked"));
                return;
            }
        }

        String openMethod = configManager.getOpenMethod().toLowerCase();

        if (openMethod.equals("shift")) {
            if (!player.isSneaking()) {
                return;
            }
            event.setCancelled(true);
            openShulker(player, item);
            return;
        }

        if (openMethod.equals("sneak")) {
            if (player.isSneaking()) {
                return;
            }
            event.setCancelled(true);
            openShulker(player, item);
            return;
        }

        if (openMethod.equals("shift-place")) {
            if (player.isSneaking()) {
                return;
            }
            event.setCancelled(true);
            openShulker(player, item);
            return;
        }

        if (openMethod.equals("always")) {
            event.setCancelled(true);
            openShulker(player, item);
            return;
        }

        if (configManager.isSmartPlacement()) {
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                Block clickedBlock = event.getClickedBlock();
                if (clickedBlock != null && clickedBlock.getType().isInteractable()) {
                    return;
                }
                return;
            }
            event.setCancelled(true);
            openShulker(player, item);
        } else {
            event.setCancelled(true);
            openShulker(player, item);
        }
    }

    private void openShulker(Player player, ItemStack item) {
        int slot = player.getInventory().getHeldItemSlot();
        boolean success = shulkerManager.openShulker(player, item, slot);

        if (!success) {
            player.sendMessage(configManager.getMessageManager().getMessage("shulker-open-error"));
        }
    }
}