package me.stephenminer.customitems.commands;

import me.stephenminer.customitems.CustomItems;
import me.stephenminer.customitems.builder.ItemBuilder;
import me.stephenminer.customitems.builder.ItemReader;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;


public class UpdateItem implements CommandExecutor{
    private final CustomItems plugin;

    public UpdateItem(){
        this.plugin = JavaPlugin.getPlugin(CustomItems.class);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        if (sender instanceof Player player){
            if (!player.hasPermission("itembuilder.commands.update")){
                player.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
                return false;
            }
            ItemStack hand = player.getInventory().getItemInMainHand();
            if (!canUpdate(hand.getItemMeta())){
                player.sendMessage(ChatColor.RED + "This item cannot be updated");
            }
            updateHand(player,hand);
            player.sendMessage(ChatColor.GREEN + "Updated your item!");
            return true;
        }else sender.sendMessage(ChatColor.RED + "You need to be a player to use this command!");
        return false;
    }

    private boolean canUpdate(ItemMeta meta){
        return meta.getPersistentDataContainer().has(plugin.id,PersistentDataType.STRING);
    }

    private void updateHand(Player player, ItemStack hand){
        ItemMeta meta = hand.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (!container.has(plugin.id, PersistentDataType.STRING)) return;

        String id = meta.getPersistentDataContainer().get(plugin.id, PersistentDataType.STRING);
        ItemBuilder builder = new ItemBuilder(id);
       // hand.setAmount(0);
        ItemStack item = builder.buildItem();
        ItemMeta newMeta = item.getItemMeta();
        if (container.has(plugin.durability, PersistentDataType.SHORT)){
            ItemReader reader = new ItemReader(hand, meta);
            short durability = reader.durability();
            if (newMeta.getPersistentDataContainer().has(plugin.durability, PersistentDataType.SHORT)){
                ItemReader newReader = new ItemReader(item, newMeta);
                newReader.setDurability(durability);
            }
        }else if (meta instanceof Damageable damageable && newMeta instanceof Damageable current) {
            current.setDamage(damageable.getDamage());
            item.setItemMeta(current);
        }
        item.setItemMeta(newMeta);
        player.getInventory().setItemInMainHand(item);
    }



}
