package me.stephenminer.customitems.commands;

import me.stephenminer.customitems.CustomItems;
import me.stephenminer.customitems.ItemConfig;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CreateItem implements CommandExecutor {
    private CustomItems plugin;
    public CreateItem(CustomItems plugin){
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        if (cmd.getName().equalsIgnoreCase("createitem")){
            int size = args.length;
            if (sender instanceof Player){
                Player player = (Player) sender;
                if (!player.hasPermission("itembuilder.commands.create"))
                    return false;
            }
            if (size < 2) {
                sender.sendMessage(ChatColor.GREEN + "");
                return false;
            }
            String id = args[0];
            String tempmat = args[1];
            Material mat = Material.matchMaterial(tempmat);
            if (mat == null)
                return false;
            boolean created = createItemEntry(id, mat);
            if (!created){
                sender.sendMessage(ChatColor.RED + "This item with id " + id + " already exists! Use the /itembuilder command to edit it!");
                return false;
            }
            sender.sendMessage(ChatColor.GREEN + "Created Item! To check out customizations, type /itembuilder and check out the autocompleter!");
            return true;
        }
        return false;
    }

    private boolean createItemEntry(String id, Material material){
        ItemConfig exists = plugin.findConfig(id);
        if (exists == null){
            ItemConfig config = new ItemConfig(plugin, id);
            config.getConfig().set("material", material.name());
            config.saveConfig();
            return true;
        }else return false;

    }
}