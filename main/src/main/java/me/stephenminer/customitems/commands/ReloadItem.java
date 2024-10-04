package me.stephenminer.customitems.commands;

import me.stephenminer.customitems.CustomItems;
import me.stephenminer.customitems.ItemConfig;
import me.stephenminer.customitems.builder.GunBuilder;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Currently (and probably forever) this command is only for gun-type items if you change certain gun statlines
 * Due to nature of other kinds of items this is less realistic since it would require me caching all the data outside of the item
 * which I can only really justify doing for the guns.
 */
public class ReloadItem implements CommandExecutor, TabCompleter {
    private final CustomItems plugin;

    public ReloadItem() {
        this.plugin = JavaPlugin.getPlugin(CustomItems.class);
    }


    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("itembuidler.cmds.reload")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
            return false;
        }
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "You need to specify the item file you wish to reload");
            return false;
        }
        ItemConfig file = plugin.findConfig(args[0]);
        GunBuilder builder = new GunBuilder(args[0], file);
        builder.loadGunAttributes(args[0]);
        sender.sendMessage(ChatColor.GREEN + "Item Stats updated where applicable (Only gun stats)");
        return true;
    }


    private boolean itemExists(String id) {
        return plugin.findConfig(id) == null;
    }


    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        int length = args.length;
        if (length == 1) return itemCompleter(args[0]);
        return null;
    }


    private List<String> itemCompleter(String match) {
        File parent = new File(plugin.getDataFolder().getPath(), "items");
        if (!parent.exists()) parent.mkdir();
        List<String> items = new ArrayList<>();
        String[] fileNames = parent.list();
        for (String name : fileNames){
            items.add(name.replace(".yml",""));
        }
        return plugin.filter(items, match);
    }
}
