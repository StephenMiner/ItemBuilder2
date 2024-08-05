package me.stephenminer.customitems.builder;

import me.stephenminer.customitems.CustomItems;
import me.stephenminer.customitems.ItemConfig;
import org.bukkit.Bukkit;
import org.bukkit.FeatureFlag;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.packs.DataPack;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Use to read an ItemConfig and apply any Armor Trims to a given ItemMeta if possible
 */
public class TrimBuilder {
    private final CustomItems plugin;
    private final ItemMeta meta;
    private final ItemConfig config;

    public TrimBuilder(ItemMeta meta, ItemConfig config){
        this.plugin = JavaPlugin.getPlugin(CustomItems.class);
        this.meta = meta;
        this.config = config;
    }

    /**
     * Loads an armor trim from the given config file
     * @return ArmorTrim as read in the config file, formated in the config as "TrimPattern,TrimMaterial"
     */
    public ArmorTrim loadTrim(){
        String boxed = config.getConfig().getString("trim");
        if (boxed == null) return null;
        String[] unbox = boxed.split(",");
        TrimPattern pattern = readPattern(unbox[0]);
        TrimMaterial material = readMaterial(unbox[1]);
        return new ArmorTrim(material, pattern);
    }

    public void applyTrim(){
        ArmorTrim trim = loadTrim();
        if (trim == null) return;

        ((ArmorMeta) meta).setTrim(trim);
    }



    /**
     * Checks if the item is one that can have an armor trim (is it armor...)
     * @return true if the meta is ArmorMeta, false otherwise
     */
    public boolean validMeta(){ return meta instanceof ArmorMeta; }

    /**
     * Reads a string to chuck out a TrimPattern
     * @param key A NamespacedKey key for a TrimPattern (What does minecraft call the trim?)
     * @return a TrimPattern corresponding to the given key
     */
    private TrimPattern readPattern(String key){
        return switch (key){
            case "sentry" -> TrimPattern.SENTRY;
            case "dune" -> TrimPattern.DUNE;
            case "coast" -> TrimPattern.COAST;
            case "wild" -> TrimPattern.WILD;
            case "ward" -> TrimPattern.WARD;
            case "eye" -> TrimPattern.EYE;
            case "vex" -> TrimPattern.VEX;
            case "tide" -> TrimPattern.TIDE;
            case "snout" -> TrimPattern.SNOUT;
            case "rib" -> TrimPattern.RIB;
            case "spire" -> TrimPattern.SPIRE;
            case "wayfinder" -> TrimPattern.WAYFINDER;
            case "shaper" -> TrimPattern.SHAPER;
            case "silence" -> TrimPattern.SILENCE;
            case "raiser" -> TrimPattern.RAISER;
            case "host" -> TrimPattern.HOST;
            default -> customPattern(key);
        };
    }

    /**
     * Attempt to get a custom TrimPattern from the minecraft registry
     * @param nameString the namespace key for the trim "namespace:key"
     * @return a TrimPattern if one corresponds to the given String, null otherwise
     */
    private TrimPattern customPattern(String nameString){
        NamespacedKey key = NamespacedKey.fromString(nameString);
        if (key == null) return null;
        Registry<TrimPattern> patternReg = Bukkit.getRegistry(TrimPattern.class);
        if (patternReg == null) return null;
        return patternReg.get(key);
    }


    /**
     * Reads a string to throw out a TrimMaterial
     * @param key A NamespacedKey key for a TrimMaterial (what does minecraft call it?)
     * @return a TrimMaterial corresponding to the given key
     */
    private TrimMaterial readMaterial(String key){
        key = key.toLowerCase();
        return switch (key){
            case "redstone" -> TrimMaterial.REDSTONE;
            case "iron" -> TrimMaterial.IRON;
            case "diamond" -> TrimMaterial.DIAMOND;
            case "quartz" -> TrimMaterial.QUARTZ;
            case "netherite" -> TrimMaterial.NETHERITE;
            case "copper" -> TrimMaterial.COPPER;
            case "gold" -> TrimMaterial.GOLD;
            case "emerald" -> TrimMaterial.EMERALD;
            case "lapis" -> TrimMaterial.LAPIS;
            case "amethyst" -> TrimMaterial.AMETHYST;
            default -> TrimMaterial.AMETHYST;
        };
    }

    /**
     * Attempt to get a custom TrimMaterial from the bukkit registry
     * @param nameString the namespace key for the trim material "namespace:key"
     * @return a TrimMaterial if one corresponds to the given String, null otherwise
     */
    private TrimMaterial customMaterial(String nameString){
        NamespacedKey key = NamespacedKey.fromString(nameString);
        if (key == null) return null;
        Registry<TrimMaterial> patternReg = Bukkit.getRegistry(TrimMaterial.class);
        if (patternReg == null) return null;
        return patternReg.get(key);
    }
}
