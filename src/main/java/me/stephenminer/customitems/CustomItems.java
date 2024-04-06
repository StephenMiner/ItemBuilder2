package me.stephenminer.customitems;

import me.stephenminer.customitems.builder.RecipeBuilder;
import me.stephenminer.customitems.commands.AutoComplete;
import me.stephenminer.customitems.commands.CreateItem;
import me.stephenminer.customitems.commands.CreateItemCompleter;
import me.stephenminer.customitems.commands.ItemBuilderCmds;
import me.stephenminer.customitems.inventories.InventoryEvents;
import me.stephenminer.customitems.listeners.HandleMelee;
import org.bukkit.ChatColor;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class CustomItems extends JavaPlugin {

    public ConfigFile Recipes;
    public ConfigFile Items;

    @Override
    public void onEnable(){
        Recipes = new ConfigFile(this, "recipes");
        Items = new ConfigFile(this, "items");
        registerCommands();
        addRecipes();
        registerEvents();

    }
    @Override
    public void onDisable(){
        Recipes.saveConfig();
        Items.saveConfig();
    }
    private void registerCommands(){
        getCommand("itembuilder").setExecutor(new ItemBuilderCmds(this));
        getCommand("itembuilder").setTabCompleter(new AutoComplete(this));
        getCommand("createitem").setExecutor(new CreateItem(this));
        getCommand("createitem").setTabCompleter(new CreateItemCompleter());

    }
    private void registerEvents(){
        PluginManager pm = getServer().getPluginManager();
      //  pm.registerEvents(new InventoryEvents(this), this);
        pm.registerEvents(new HandleMelee(), this);
    }
    private void addRecipes(){
        new BukkitRunnable(){
            final CustomItems plugin = CustomItems.this;
            @Override
            public void run(){
                if (!Recipes.getConfig().contains("recipes"))
                    return;
                for (String id : Recipes.getConfig().getConfigurationSection("recipes").getKeys(false)){
                    if (id == null || id.isEmpty())
                        continue;
                    RecipeBuilder rb = new RecipeBuilder(plugin, id);
                    rb.createRecipe(id);
                }
            }
        }.runTaskLater(this, 2);
    }

    public ItemConfig findConfig(String id){
        File parent = new File(this.getDataFolder().getPath(), "items");
        if (!parent.exists()) parent.mkdir();
        File child = new File(parent, id + ".yml");
        if (child.exists()) return new ItemConfig(this, id);
        else return null;
    }
    public List<String> filter(Collection<String> base, String match){
        if (match == null || match.isEmpty()) return new ArrayList<>(base);
        match = match.toLowerCase();
        List<String> filtered = new ArrayList<>();
        for (String entry : base){
            String temp = ChatColor.stripColor(entry).toLowerCase();
            if (temp.contains(match)) filtered.add(entry);
        }
        return filtered;
    }
}
