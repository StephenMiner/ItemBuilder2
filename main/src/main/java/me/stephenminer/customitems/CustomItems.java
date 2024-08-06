package me.stephenminer.customitems;

import me.stephenminer.customitems.builder.RecipeBuilder;
import me.stephenminer.customitems.commands.*;
import me.stephenminer.customitems.listeners.*;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
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
 * 1.21
 */

public final class CustomItems extends JavaPlugin {
    //Just the id for the item
    public NamespacedKey id;
    //Defines the melee range for the item
    public NamespacedKey reach;
    //Defines whether debuffs will be recieved for not having a free offhand while using the item
    public NamespacedKey twoHanded;
    //Defines the damage multiplier received for using this weapon while on a mount
    public NamespacedKey mounted;
    //Defines the amount of ticks this item can disable shields for upon a fully charged attack
    public NamespacedKey shieldbreaker;
    //Defines whether this item is a gun or not (with its presence) and what kind of projectile pattern the gun will have
    public NamespacedKey gun;
    //Defines the damage for gun projectiles
    public NamespacedKey gunDamage;
    //Defines the item that can be used for ammo
    public NamespacedKey ammo;
    //Defines the max range for this gun
    public NamespacedKey range;
    //defines the max range before damage begins to decay for gun projectiles
    public NamespacedKey decay;
    //defines the decay rate of damage for gun projectiles after hitting the decay range
    public NamespacedKey decayRate;
    //defines how long it will take for the "ramrod" firing stage to complete
    public NamespacedKey ramTime;
    //defines what item can be used as powder in the "load powder" firing stage
    public NamespacedKey powder;
    //defines how many projectiles will be shot. Only applies to gun types with a SPREAD firing pattern
    public NamespacedKey projectiles;
    //Defines the cooldown the gun will recieve when it is shot (in ticks)
    public NamespacedKey triggerCooldown;
    //Not saved on items
    public NamespacedKey dummy;
    //Makes an item unstackable
    public NamespacedKey unstackable;
    //Not a value users can write
    public NamespacedKey material;
    //Defines the cooldown the gun will recieve (in ticks) when it is equipped
    public NamespacedKey equipCooldown;
    //
    public NamespacedKey gunIgnoreArmor;

    public NamespacedKey ignoreArmor;
    //Defines the damage bonus vs players
    public NamespacedKey playerBonus;
    //Defines the damage bonus vs mobs
    public NamespacedKey mobBonus;
    //Defines whether damage bonus attributes should apply to melee attacks done by guns or not (default = false)
    public NamespacedKey rangedMelee;
    //Defines how wide the arc of spread should be. Only for guns of type Spread
    public NamespacedKey gunSpread;
    //Defines whether the item can be placed or not. Only applicable if the item is a block.
    public NamespacedKey placeable;
    //Defines a custom durability for an item
    //Stored as a short, max = 32k
    public NamespacedKey durability;
    //Defines a custom max durability for an item (does not support anvil repairing or mending)
    //Stored as a short, max = 32k
    public NamespacedKey maxUses;
    //The damage decay rate for projectiles in water
    public NamespacedKey waterDecay;


    public ConfigFile Recipes;
    public ConfigFile Items;


    public FixedMetadataValue bulletHit;

    @Override
    public void onEnable(){
        Recipes = new ConfigFile(this, "recipes");
        Items = new ConfigFile(this, "items");
        bulletHit = new FixedMetadataValue(this, true);

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
        mobBonus = new NamespacedKey(this,"mobbonus");
        playerBonus = new NamespacedKey(this, "playerbonus");
        ignoreArmor = new NamespacedKey(this,"ap");
        gunSpread = new NamespacedKey(this, "gunspread");
        placeable = new NamespacedKey(this,"placeable");
        rangedMelee = new NamespacedKey(this,"rangemelee");
        durability = new NamespacedKey( this, "use");
        maxUses = new NamespacedKey(this,"maxuse");
        waterDecay = new NamespacedKey(this,"waterdecay");
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
        getCommand("forceitems").setExecutor(new ForceItems());

    }
    private void registerEvents(){
        PluginManager pm = getServer().getPluginManager();
      //  pm.registerEvents(new InventoryEvents(this), this);
        pm.registerEvents(new HandleMelee(), this);
        pm.registerEvents(new TwoHandedListener(),this);
        pm.registerEvents(new ShieldListener(), this);
        pm.registerEvents(new GunListener(),this);
        pm.registerEvents(new ItemListener(),this);
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

    public boolean hasID(ItemMeta meta, String id){
        if (meta == null) return false;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        String savedId = container.getOrDefault(this.id, PersistentDataType.STRING, "");
        return savedId.equalsIgnoreCase(id);
    }
}
