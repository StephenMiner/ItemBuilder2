package me.stephenminer.customitems;

import me.stephenminer.customitems.builder.RecipeBuilder;
import me.stephenminer.customitems.commands.AutoComplete;
import me.stephenminer.customitems.commands.CreateItem;
import me.stephenminer.customitems.commands.CreateItemCompleter;
import me.stephenminer.customitems.commands.ItemBuilderCmds;
import me.stephenminer.customitems.listeners.GunListener;
import me.stephenminer.customitems.listeners.HandleMelee;
import me.stephenminer.customitems.listeners.ShieldListener;
import me.stephenminer.customitems.listeners.TwoHandedListener;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * SUPPORTED VERSIONS
 * 1.20/1.20.1
 * 1.20.2
 * 1.20.4
 */

public final class CustomItems extends JavaPlugin {
    public NamespacedKey id;
    public NamespacedKey reach;
    public NamespacedKey twoHanded;
    public NamespacedKey mounted;
    public NamespacedKey shieldbreaker;
    public NamespacedKey gun;
    public NamespacedKey gunDamage;
    public NamespacedKey ammo;
    public NamespacedKey range;
    public NamespacedKey decay;
    public NamespacedKey decayRate;
    public NamespacedKey ramTime;
    public NamespacedKey powder;
    public NamespacedKey projectiles;
    public NamespacedKey triggerCooldown;
    public NamespacedKey dummy;
    public NamespacedKey unstackable;
    public NamespacedKey material;
    public NamespacedKey equipCooldown;
    public NamespacedKey gunIgnoreArmor;
    public ConfigFile Recipes;
    public ConfigFile Items;

    @Override
    public void onEnable(){
        Recipes = new ConfigFile(this, "recipes");
        Items = new ConfigFile(this, "items");

        createAttributeKeys();
        registerCommands();
        addRecipes();
        registerEvents();

    }


    private void createAttributeKeys(){
        reach = new NamespacedKey(this,"reach");
        twoHanded = new NamespacedKey(this,"twohanded");
        mounted = new NamespacedKey(this,"mounted");
        shieldbreaker = new NamespacedKey(this, "shieldbreaker");
        gun = new NamespacedKey(this, "gun");
        ammo = new NamespacedKey(this, "ammo");
        range = new NamespacedKey(this, "range");
        decayRate = new NamespacedKey(this, "decayrate");
        decay = new NamespacedKey(this,"decay" );
        id = new NamespacedKey(this,"id");
        dummy = new NamespacedKey(this,"dummy");
        gunDamage = new NamespacedKey(this,"gundamage");
        ramTime = new NamespacedKey(this, "ramtime");
        powder = new NamespacedKey(this, "powder");
        projectiles = new NamespacedKey(this,"prjectiles");
        triggerCooldown = new NamespacedKey(this,"triggercooldown");
        material = new NamespacedKey(this,"mat");
        unstackable = new NamespacedKey(this, "nostack");
        equipCooldown = new NamespacedKey(this,"equipcd");
        gunIgnoreArmor = new NamespacedKey( this, "gunap");


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
        pm.registerEvents(new TwoHandedListener(),this);
        pm.registerEvents(new ShieldListener(), this);
        pm.registerEvents(new GunListener(),this);
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
        return null;
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
