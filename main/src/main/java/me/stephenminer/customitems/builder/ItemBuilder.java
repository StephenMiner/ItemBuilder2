package me.stephenminer.customitems.builder;


import me.stephenminer.customitems.CustomItems;
import me.stephenminer.customitems.ItemConfig;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class ItemBuilder {
    private final CustomItems plugin;
    private final ItemConfig config;
    private String id;

    public ItemBuilder(CustomItems plugin, String id) {
        this.plugin = plugin;
        this.id = id;
        this.config = plugin.findConfig(id);
    }
    public ItemBuilder(String id){
        this(JavaPlugin.getPlugin(CustomItems.class), id);
    }

    public boolean hasEntry(){ return config != null; }

    public boolean isGun(){
        return config.getConfig().contains("gun-type");
    }

    public boolean hasFoodComp(){ return config.getConfig().contains("food"); }

    private List<String> getStringList(String section) {
        if (!config.getConfig().contains(section))
            return null;
        return config.getConfig().getStringList(section);
    }

    private Set<String> getConfigurationSection(String section) {
        if (!config.getConfig().contains(section))
            return null;
        return config.getConfig().getConfigurationSection(section).getKeys(false);
    }

    private String getDisplayName() {
        if (!config.getConfig().contains("display-name"))
            return null;
        String name = config.getConfig().getString("display-name");
        if (name == null)
            return null;
        return ChatColor.translateAlternateColorCodes('&', name);
    }

    private List<String> getLore() {
        List<String> tempList = getStringList("lore");
        List<String> returnList = new ArrayList<>();
        if (tempList == null)
            tempList = new ArrayList<>();
        if (tempList.size() > 0)
            for (String entry : tempList) {
                if (entry != null && !entry.isEmpty())
                    returnList.add(ChatColor.translateAlternateColorCodes('&', entry));
            }
        //returnList.add(ChatColor.GRAY + id);
        return returnList;
    }

    public Map<Enchantment, Integer> getEnchantments() {
        Set<String> section = getConfigurationSection("enchantments");
        Map<Enchantment, Integer> enchants = new HashMap<>();
        if (section == null)
            return null;
        for (String key : section) {
            if (key == null)
                continue;
            Enchantment enchant = Enchantment.getByKey(NamespacedKey.minecraft(key));
            int level = config.getConfig().getInt("enchantments." + key + ".level");
            if (level < 1) {
                enchants.put(enchant, 1);
                continue;
            }
            enchants.put(enchant, level);
        }
        return enchants;
    }

    public ItemFlag[] getFlags() {
        List<String> flags = getStringList("item-flags");
        List<ItemFlag> returnList = new ArrayList<>();
        if (flags == null)
            return null;
        for (String key : flags) {
            if (key == null)
                continue;
            returnList.add(ItemFlag.valueOf(key));
        }
        return returnList.toArray(new ItemFlag[0]);
    }

    private Material getMaterial() {
        String sMat = config.getConfig().getString("material");
        Material mat = Material.matchMaterial(sMat);
        if (mat == null)
            return Material.FEATHER;
        return mat;
    }

    private boolean getUnbreakable() {
        if (!config.getConfig().contains("unbreakable"))
            return false;
        boolean unbreakable = config.getConfig().getBoolean("unbreakable");
        return unbreakable;
    }

    private int getReach(){
        if (!config.getConfig().contains("reach")) return -1;
        return config.getConfig().getInt("reach");
    }

    private boolean twoHanded(){
        //if (!config.getConfig().contains("two-handed")) return false;
        return config.getConfig().getBoolean("two-handed");
    }

    private float playerBonus() {
        return (float) config.getConfig().getDouble("player-bonus");
    }
    private float mobBonus(){
        return (float) config.getConfig().getDouble("mob-bonus");
    }

    private boolean hasCustomModelData(){
        return config.getConfig().contains("custom-model-data");
    }
    private int getCustomModelData(){
        return config.getConfig().getInt("custom-model-data");
    }

    private double mountMultiplier(){
        if (!config.getConfig().contains("mount-multiplier")) return 1;
        else return config.getConfig().getInt("mount-multiplier");
    }

    private int shieldBreakerTicks(){
        if (!config.getConfig().contains("shield-breaker-ticks")) return -1;
        else return config.getConfig().getInt("shield-breaker-ticks");
    }
    private int triggerCooldown(){
        if (!config.getConfig().contains("trigger-cooldown")) return -1;
        else return config.getConfig().getInt("trigger-cooldown");
    }
    private boolean unstackable(){
        return config.getConfig().getBoolean("unstackable");
    }

    private boolean rangeMelee(){
        return config.getConfig().getBoolean("range-melee");
    }
    private boolean placeable(){ return config.getConfig().getBoolean("placeable"); }

    private float ap(){
        if (config.getConfig().contains("melee-ap")) {
            return (float) config.getConfig().getDouble("melee-ap");
        }else return 1;
    }

    public short maxUses(){ return (short) config.getConfig().getInt("max-durability"); }

    public boolean canEnchant(){
        if (!config.getConfig().contains("enchantable")) return true;
        return config.getConfig().getBoolean("enchantable");
    }

    private int maxStackSize(){ return config.getConfig().getInt("stack-size"); }


    private ItemStack constructFoodComps(ItemStack item, ItemMeta meta){
        if (!hasFoodComp() || !CustomItems.foodComps) return null;
        String[] foodComps = new String[5];
        foodComps[0] = config.getConfig().getString("food.stats");
        List<String> effects = config.getConfig().getStringList("food.effects");
        StringBuilder flattenEffects = new StringBuilder();
        for (String effect : effects) flattenEffects.append(effect).append("/");
        if (!flattenEffects.isEmpty()) flattenEffects.deleteCharAt(flattenEffects.length()-1);
        foodComps[1] = flattenEffects.toString();
        foodComps[2] = config.getConfig().getString("food.eat-seconds");
        foodComps[3] = config.getConfig().getString("food.always-eat");
        boolean nms21 = plugin.version[1] == 21 && plugin.version[2] >= 3;
        String packageName = "me.stephenminer";
        try{
            FoodBuilder builder;
            if ((plugin.version[1] == 20 && plugin.version[2] >= 5) || !nms21)
                builder = (FoodBuilder) Class.forName(packageName + ".v1_21_R1.FoodParser").getConstructor(ItemStack.class, ItemMeta.class,String[].class).newInstance(item, meta, foodComps);
            else{
                builder = (FoodBuilder) Class.forName(packageName + ".v1_21_R3.FoodParser").getConstructor(ItemStack.class, String[].class).newInstance(item, foodComps);
            }
            return builder.build();
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return null;
    }



    private ItemMeta addCustomTags(ItemMeta meta){
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(plugin.id,PersistentDataType.STRING,id);
        container.set(plugin.material,PersistentDataType.STRING, getMaterial().name());
        boolean unstackable = unstackable();
        if (unstackable) container.set(plugin.unstackable,PersistentDataType.STRING,UUID.randomUUID().toString());
        if (twoHanded())
            container.set(plugin.twoHanded, PersistentDataType.BOOLEAN, true);

        int reach = getReach();
        if (reach > 0)
            container.set(plugin.reach,PersistentDataType.INTEGER,reach);

        double multiplier = mountMultiplier();
        if (multiplier != 1)
            container.set(plugin.mounted,PersistentDataType.DOUBLE,multiplier);

        int breakerTicks = shieldBreakerTicks();
        if (breakerTicks != -1)
            container.set(plugin.shieldbreaker,PersistentDataType.INTEGER,breakerTicks);
        int triggerCooldown = triggerCooldown();
        if (triggerCooldown != -1)
            container.set(plugin.triggerCooldown,PersistentDataType.INTEGER,triggerCooldown);
        float playerBonus = playerBonus();
        if (playerBonus != 0)
            container.set(plugin.playerBonus,PersistentDataType.FLOAT,playerBonus);
        float mobBonus = mobBonus();
        if (mobBonus != 0)
            container.set(plugin.mobBonus,PersistentDataType.FLOAT,mobBonus);
        if (rangeMelee())
            container.set(plugin.rangedMelee,PersistentDataType.BOOLEAN,true);
        float ap = ap();
        if (ap != 1)
            container.set(plugin.ignoreArmor,PersistentDataType.FLOAT,ap);
        boolean placeable = placeable();
        if (!placeable) container.set(plugin.placeable,PersistentDataType.BOOLEAN,false);


        boolean enchantable = canEnchant();
        if (!enchantable)
            container.set(plugin.enchantable,PersistentDataType.BOOLEAN,false);
        meta = addComponentTags(meta);
        return meta;
    }

    private ItemMeta addComponentTags(ItemMeta meta){
        short maxUses = maxUses();
        if (maxUses > 0) {
            if (plugin.durHandler == null){
                PersistentDataContainer container = meta.getPersistentDataContainer();
                container.set(plugin.maxUses,PersistentDataType.SHORT,maxUses);
                container.set(plugin.durability,PersistentDataType.SHORT,maxUses);
            }else meta = plugin.durHandler.addDurability(meta,maxUses);
        }
        int stackSize = maxStackSize();
        if (stackSize > 0 && plugin.stackHandler != null){
            meta = plugin.stackHandler.addStackSize(meta, stackSize);
        }
        return meta;
    }


    public ItemStack buildItem() {
        Material mat = getMaterial();
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (hasCustomModelData())
            meta.setCustomModelData(getCustomModelData());
        meta.setDisplayName(getDisplayName());
        List<String> lore = getLore();
        if (lore.isEmpty() && maxUses() > 0) lore = new ArrayList<>();
        if (maxUses() > 0)
            lore.add(ChatColor.GRAY + "Uses: " + maxUses());
        if (!lore.isEmpty())
            meta.setLore(lore);
        Map<Enchantment, Integer> map = getEnchantments();
        if (map != null && !map.isEmpty())
            for (Enchantment entry : map.keySet()) {
                meta.addEnchant(entry, map.get(entry), true);
            }
        if (getFlags() != null && getFlags().length != 0)
            meta.addItemFlags(getFlags());
        meta.setUnbreakable(getUnbreakable());
        meta = addCustomTags(meta);

        GunBuilder gunBuilder = new GunBuilder(id, config);
        gunBuilder.loadGunAttributes(id);
       // meta = gunBuilder.buildGunAttributes(mat, meta);
        TrimBuilder builder = new TrimBuilder(meta,config);
        if (builder.validMeta())
            builder.applyTrim();

        item.setItemMeta(meta);
        BuildAttribute ba = new BuildAttribute(plugin, config);
        ItemStack newItem = ba.addAttributes(item);
        ItemStack withComps = constructFoodComps(newItem,newItem.getItemMeta());
        return withComps==null ? newItem : withComps;
    }

    public ItemConfig getConfig(){ return config; }
}