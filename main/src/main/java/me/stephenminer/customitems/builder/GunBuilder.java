package me.stephenminer.customitems.builder;

import me.stephenminer.customitems.CustomItems;
import me.stephenminer.customitems.ItemConfig;
import me.stephenminer.customitems.gunutils.GunReader;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Meant to be used as a companion for the ItemBuilder class so that it doesn't get so cluttered
 */
public class GunBuilder {
    private final String id;
    private final ItemConfig file;
    private final CustomItems plugin;

    public GunBuilder(String id, ItemConfig file){
        this.plugin = JavaPlugin.getPlugin(CustomItems.class);
        this.id = id;
        this.file = file;
    }


    public boolean isGun(){
        return file.getConfig().contains("gun-type");
    }

    private String gunType(){
        return file.getConfig().getString("gun-type").toLowerCase();
    }

    private double loadDamage(){
        return file.getConfig().getDouble("gun-damage");
    }

    private int loadRamTime(){
        return file.getConfig().getInt("ram-time");
    }
    private double loadRange(){
        return file.getConfig().getInt("range");
    }
    private double loadDecay(){
        if (file.getConfig().contains("decay-range"))
            return file.getConfig().getDouble("decay-range");
        return -1;
    }
    //Can only be used in conjunction with decay-range
    private double loadDecayRate(){
        if (file.getConfig().contains("decay-rate")) return file.getConfig().getDouble("decay-rate");
        return 0;
    }
    //Should only be used for 'spread' type weapons
    private int loadProjectiles(){
        if (!file.getConfig().contains("projectiles")) return 1;
        else return file.getConfig().getInt("projectiles");
    }
    private String loadAmmoId(){
        return file.getConfig().getString("ammo");
    }
    private String loadPowderId(){
        return file.getConfig().getString("powder");
    }

    private int equipCooldown(){
        if (!file.getConfig().contains("equip-cooldown")) return -1;
        else return file.getConfig().getInt("equip-cooldown");
    }

    private float ignoreArmor(){
        if (!file.getConfig().contains("gunap")) return 1;
        else return (float) Math.min(1,file.getConfig().getDouble("gunap"));
    }


    /**
     *
     * @param meta the ItemMeta to write to
     * @return ItemMeta with tags for gun if any are in the config
     */
    public ItemMeta buildGunAttributes(Material type, ItemMeta meta){
        if (!isGun()) return meta;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        String gunType = gunType();
        container.set(plugin.gun, PersistentDataType.STRING,gunType);
        if (gunType.equals("spread")){
            int projectiles = loadProjectiles();
            container.set(plugin.projectiles,PersistentDataType.INTEGER,projectiles);
        }

        double dmg = loadDamage();
        if (dmg > 0) container.set(plugin.gunDamage,PersistentDataType.DOUBLE,dmg);

        double range = loadRange();
        if (range > 0) container.set(plugin.range,PersistentDataType.DOUBLE,range);

        double decayRange = loadDecay();
        double decayRate = loadDecayRate();
        if (decayRange > -1 && decayRate > 0){
            container.set(plugin.decay, PersistentDataType.DOUBLE, decayRange);
            container.set(plugin.decayRate, PersistentDataType.DOUBLE,decayRate);
        }

        String ammo = loadAmmoId();
        if (ammo != null) container.set(plugin.ammo,PersistentDataType.STRING,ammo);

        String powder = loadPowderId();
        if (powder != null) container.set(plugin.powder, PersistentDataType.STRING, powder);

        int ramTime = loadRamTime();
        if (ramTime > 0) container.set(plugin.ramTime,PersistentDataType.INTEGER,ramTime);

        int equipCooldown = equipCooldown();
        if (equipCooldown != -1)
            container.set(plugin.equipCooldown,PersistentDataType.INTEGER,equipCooldown);
        float gunAp = ignoreArmor();
        if (gunAp != 1)
            container.set(plugin.gunIgnoreArmor,PersistentDataType.FLOAT,gunAp);
        GunReader reader = new GunReader(null, meta);
        String defaultStage = reader.getFiringStage();
        List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
        lore.add(0,ChatColor.YELLOW + "" + defaultStage);
        meta.setLore(lore);
        if (!defaultStage.equals("ready to fire")){
            Damageable damageable = (Damageable) meta;
            damageable.setDamage(type.getMaxDurability()-1);
        }
        return meta;
    }



    public enum GunType{
        LINE("line"),
        SPREAD("spread");

        private GunType(String id){
            this.id = id;
        }
        private final String id;
        public String id(){ return id; }
    }
}
