package me.stephenminer.customitems;

import me.stephenminer.customitems.builder.GunBuilder;
import me.stephenminer.customitems.builder.ItemBuilder;
import me.stephenminer.customitems.builder.RecipeBuilder;
import me.stephenminer.customitems.commands.*;
import me.stephenminer.customitems.gunutils.GunRecord;
import me.stephenminer.customitems.listeners.*;
import org.bukkit.Bukkit;
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
    public static boolean foodComps;
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
    //the size of the bullet;
    public NamespacedKey bulletSize;
    //the amount of entities a bullet can pierce
    public NamespacedKey pierce;
    public NamespacedKey enchantable;

    public NamespacedKey slowRam;
    public NamespacedKey gunOffhand;


    public ConfigFile Recipes;
    public ConfigFile Items;


    public FixedMetadataValue bulletHit;

    @Override
    public void onEnable(){
        Recipes = new ConfigFile(this, "recipes");
        Items = new ConfigFile(this, "items");
        bulletHit = new FixedMetadataValue(this, true);

        String ver = Bukkit.getServer().getBukkitVersion();
        ver = ver.substring(0, ver.indexOf("-"));
        String[] comps = ver.split("\\.");
        int major = Integer.parseInt(comps[1]);
        if (major == 20){
            foodComps = comps.length > 2 && Integer.parseInt(comps[2]) > 5;
        }else if (major > 20) CustomItems.foodComps = true;
        this.version = versionComponents();



        createAttributeKeys();
        registerCommands();
        addRecipes();
        registerEvents();
        createGunRecords();

    }

    private void createGunRecords(){
        File parent = new File(this.getDataFolder().getPath(), "items");
        if (!parent.exists()) parent.mkdir();
        List<String> items = names();
        for (String name : items){
            ItemBuilder builder = new ItemBuilder(name);
            if (builder.hasEntry() && builder.isGun()){
                GunBuilder gunBuilder = new GunBuilder(name, builder.getConfig());
                gunBuilder.loadGunAttributes(name);
            }
        }
    }

    private List<String> names() {
        File parent = new File(this.getDataFolder().getPath(), "items");
        if (!parent.exists()) parent.mkdir();
        List<String> items = new ArrayList<>();
        String[] fileNames = parent.list();
        for (String name : fileNames){

            if (!name.contains(".yml")) continue;
            items.add(name.replace(".yml",""));
        }
        return items;
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
        bulletSize = new NamespacedKey(this,"bsize");
        pierce = new NamespacedKey(this,"pierce");
        enchantable = new NamespacedKey( this, "enchantable");
        slowRam = new NamespacedKey(this, "slowram");
        gunOffhand = new NamespacedKey(this, "gunoffhand");


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
        ReloadItem reloadItem = new ReloadItem();
        getCommand("reloaditem").setExecutor(reloadItem);
        getCommand("reloaditem").setTabCompleter(reloadItem);
        getCommand("updateitem").setExecutor(new UpdateItem());

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

    public int[] version;

    public int[] versionComponents(){
        String ver = Bukkit.getServer().getBukkitVersion();
        ver = ver.substring(0, ver.indexOf("-"));
        String[] comps = ver.split("\\.");
        int size = comps.length;
        int[] vercomps = new int[size];
        for (int i = 0; i < size; i++ ){
            vercomps[i] = Integer.parseInt(comps[i]);
        }
        return vercomps;
    }
}
