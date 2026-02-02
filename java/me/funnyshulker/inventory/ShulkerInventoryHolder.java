package me.funnyshulker.inventory;

import me.funnyshulker.FunnyShulker;
import me.funnyshulker.model.ShulkerData;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class ShulkerInventoryHolder implements InventoryHolder {

    private final ShulkerData shulkerData;
    private final Inventory inventory;

    public ShulkerInventoryHolder(ShulkerData shulkerData) {
        this.shulkerData = shulkerData;
        this.inventory = Bukkit.createInventory(this, 27, getInventoryName());
    }

    private String getInventoryName() {
        String name = shulkerData.getShulkerItem().getItemMeta() != null &&
                shulkerData.getShulkerItem().getItemMeta().hasDisplayName() ?
                shulkerData.getShulkerItem().getItemMeta().getDisplayName() :
                shulkerData.getShulkerItem().getType().toString();

        return FunnyShulker.getInstance().getConfigManager().getMessageManager()
                .getMessage("shulker-name").replace("{name}", name);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public ShulkerData getShulkerData() {
        return shulkerData;
    }
}