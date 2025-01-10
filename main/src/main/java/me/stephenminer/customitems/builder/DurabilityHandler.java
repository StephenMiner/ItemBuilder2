package me.stephenminer.customitems.builder;

import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

public interface DurabilityHandler {

    public Damageable addDurability(ItemMeta meta, int maxUses);
}
