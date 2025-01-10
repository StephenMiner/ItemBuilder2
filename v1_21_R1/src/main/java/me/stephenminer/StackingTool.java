package me.stephenminer;

import me.stephenminer.customitems.builder.StackingHandler;
import org.bukkit.inventory.meta.ItemMeta;

public class StackingTool implements StackingHandler
{
    @Override
    public ItemMeta addStackSize(ItemMeta meta, int stackSize) {
        meta.setMaxStackSize(stackSize);
        return meta;
    }
}
