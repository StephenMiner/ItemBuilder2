package me.stephenminer.customitems.builder;

import me.stephenminer.customitems.CustomItems;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class ItemReader {
    private final ItemMeta meta;
    private final ItemStack host;

    private final CustomItems plugin;

    public ItemReader(ItemStack host, ItemMeta meta){
        this.plugin = JavaPlugin.getPlugin(CustomItems.class);
        this.host = host;
        this.meta = meta;
    }




    public short durability(){
        return meta.getPersistentDataContainer().getOrDefault(plugin.durability, PersistentDataType.SHORT, (short) -1);
    }
    public short maxDurability(){
        return meta.getPersistentDataContainer().getOrDefault(plugin.maxUses,PersistentDataType.SHORT,(short)-1);
    }

    /**
     * Used to display the custom durability on the item's durability bar
     * Sets the host's itemmeta
     */
    public void updateMinecraftDamage(){
        if (meta instanceof Damageable damageable){
            float ratio = 1 - (((float) durability()) / maxDurability());
            int dmg = (int) Math.max(host.getType().getMaxDurability() * ratio, 1);
            damageable.setDamage(dmg);
            host.setItemMeta(meta);
        }
    }



}
