package me.stephenminer;

import me.stephenminer.customitems.builder.DurabilityHandler;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * singleton class since this stuff is only present in versions 1.20.6+
 */
public class DurabilityTool implements DurabilityHandler {

    public DurabilityTool(){

    }
    @Override
    public Damageable addDurability(ItemMeta meta, int maxUses){
        Damageable damageable = (Damageable) meta;
        damageable.setMaxDamage(maxUses);
        damageable.setDamage(0);
        System.out.println(99);
        return damageable;
    }
}
