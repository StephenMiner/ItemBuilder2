package me.stephenminer.customitems.builder;

import org.bukkit.inventory.meta.ItemMeta;

public interface StackingHandler {

    public ItemMeta addStackSize(ItemMeta meta, int stackSize);
}
