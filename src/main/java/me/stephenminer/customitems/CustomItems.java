package me.stephenminer.customitems;

import me.stephenminer.customitems.builder.RecipeBuilder;
import me.stephenminer.customitems.commands.AutoComplete;
import me.stephenminer.customitems.commands.CreateItem;
import me.stephenminer.customitems.commands.CreateItemCompleter;
import me.stephenminer.customitems.commands.ItemBuilderCmds;
import me.stephenminer.customitems.inventories.InventoryEvents;
import me.stephenminer.customitems.listeners.HandleMelee;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;

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
}
