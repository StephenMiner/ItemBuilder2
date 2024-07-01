package me.stephenminer.customitems.listeners;

import me.stephenminer.customitems.CustomItems;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class ItemListener implements Listener {
    private final CustomItems plugin;


    public ItemListener(){
        this.plugin = JavaPlugin.getPlugin(CustomItems.class);
    }

    @EventHandler
    public void cancelPlace(BlockPlaceEvent event){
        Player player = event.getPlayer();
        ItemStack item = event.getItemInHand();
        if (item.getType().isAir() || !item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();
        if (!canPlace(meta)) event.setCancelled(true);
    }



    private boolean canPlace(ItemMeta meta){
        return meta.getPersistentDataContainer().getOrDefault(plugin.placeable, PersistentDataType.BOOLEAN,true);
    }
}
