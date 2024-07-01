package me.stephenminer.customitems.commands;

import me.stephenminer.customitems.CustomItems;
import me.stephenminer.customitems.builder.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class ForceItems implements CommandExecutor {
    private final CustomItems plugin;

    public ForceItems(){
        this.plugin = JavaPlugin.getPlugin(CustomItems.class);
    }


    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        if (!sender.hasPermission("itembuilder.commands.forceitems")){
            sender.sendMessage(ChatColor.RED + "You do not have permission for this command!");
            return false;
        }
        for(Player player : Bukkit.getOnlinePlayers()){
            updatePlayerItems(player);
        }
        sender.sendMessage(ChatColor.GREEN + "Updated player items");
        return true;
    }

    private void updatePlayerItems(Player player){
        ItemStack[] items = player.getInventory().getContents();
        for (int i = items.length-1; i >= 0; i--){
            ItemStack item = items[i];
            if (item == null || !item.hasItemMeta()) continue;
            PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
            if (container.has(plugin.id, PersistentDataType.STRING)){
                String id = container.get(plugin.id,PersistentDataType.STRING);
                player.getInventory().remove(item);
                int amount = item.getAmount();
                ItemStack replace = new ItemBuilder(plugin, id).buildItem();
                replace.setAmount(amount);
                player.getInventory().addItem(replace);
            }
        }
    }
}
