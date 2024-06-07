package me.stephenminer.customitems.commands;

import me.stephenminer.customitems.CustomItems;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CreateItemCompleter implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("createitem")){
            int size = args.length;
            if (size < 2)
                return null;
            return materialCompleter(args[1]);
        }
        return null;
    }
    private List<String> materialCompleter(String match){
        List<String> matList = Arrays.stream(Material.values()).map(Material::name).toList();
        return JavaPlugin.getPlugin(CustomItems.class).filter(matList,match);
    }
}