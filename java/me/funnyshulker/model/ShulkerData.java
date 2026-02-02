package me.funnyshulker.model;

import org.bukkit.inventory.ItemStack;
import java.util.UUID;

public class ShulkerData {

    private final UUID playerId;
    private final ItemStack shulkerItem;
    private final int slot;
    private final long openTime;

    public ShulkerData(UUID playerId, ItemStack shulkerItem, int slot) {
        this.playerId = playerId;
        this.shulkerItem = shulkerItem.clone();
        this.slot = slot;
        this.openTime = System.currentTimeMillis();
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public ItemStack getShulkerItem() {
        return shulkerItem;
    }

    public int getSlot() {
        return slot;
    }

    public long getOpenTime() {
        return openTime;
    }
}