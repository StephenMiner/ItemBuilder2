package me.stephenminer.customitems.builder;

import me.stephenminer.customitems.CustomItems;
import me.stephenminer.customitems.ItemConfig;
import me.stephenminer.customitems.gunutils.GunReader;
import me.stephenminer.customitems.gunutils.GunRecord;
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
        if (file.getConfig().contains("ram-time"))
            return file.getConfig().getInt("ram-time");
        else return -1;
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

    private double gunSpread(){
        if (!file.getConfig().contains("gun-spread")) return 0.25;
        else return Math.abs(file.getConfig().getDouble("gun-spread"));
    }

    private double waterDecay(){
        if (!file.getConfig().contains("water-decay")) return -1d;
        else return file.getConfig().getDouble("water-decay");
    }
    private double bulletSize(){
        if (!file.getConfig().contains("bullet-size")) return -1d;
        else return file.getConfig().getDouble("bullet-size");
    }

    private int pierce(){
        if (!file.getConfig().contains("pierce")) return -1;
        else return file.getConfig().getInt("pierce");
    }

    private boolean slowRam(){
        return file.getConfig().getBoolean("slow-ram");
    }

    private boolean gunOffhand(){
        if (!file.getConfig().contains("gun-offhand")) return true;
        return file.getConfig().getBoolean("gun-offhand");
    }

    private int triggerCooldown(){
        if (!file.getConfig().contains("trigger-cooldown")) return -1;
        else return file.getConfig().getInt("trigger-cooldown");
    }


    public void loadGunAttributes(String id){
        GunRecord record = GunRecord.IDS.getOrDefault(id,new GunRecord(id));
        record.setDamage(loadDamage());
        record.setRange(loadRange());
        record.setDecayRange(loadDecay());
        record.setDecayRate(loadDecayRate());
        record.setShot(loadAmmoId());
        record.setPowder(loadPowderId());
        record.setRamTime(loadRamTime());
        record.setEquipCd(equipCooldown());
        record.setCd(triggerCooldown());
        record.setGunap(ignoreArmor());
        record.setWaterDecay(waterDecay());
        record.setBulletSize(bulletSize());
        record.setPierce(pierce());
        record.setSlowRam(slowRam());
        record.setGunOffhand(gunOffhand());
        record.setProjectiles(loadProjectiles());
        record.setSpread(gunSpread());
    }
    /**
     *
     * @param meta the ItemMeta to write to
     * @return ItemMeta with tags for gun if any are in the config
     */
    public ItemMeta buildGunAttributes(Material type, ItemMeta meta){
        loadGunAttributes(id);
        if (!isGun()) return meta;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        String gunType = gunType();
        container.set(plugin.gun, PersistentDataType.STRING,gunType);
        if (gunType.equals("spread")){
            int projectiles = loadProjectiles();
            container.set(plugin.projectiles,PersistentDataType.INTEGER,projectiles);
            double spread = gunSpread();
            if (spread != 0.25)
                container.set(plugin.gunSpread, PersistentDataType.DOUBLE, spread);
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
        double waterDecay = waterDecay();
        if (waterDecay != -1)
            container.set(plugin.waterDecay,PersistentDataType.DOUBLE, waterDecay);
        double bulletSize = bulletSize();
        if (bulletSize != -1)
            container.set(plugin.bulletSize,PersistentDataType.DOUBLE,bulletSize);
        int pierce = pierce();
        if (pierce > 1)
            container.set(plugin.pierce,PersistentDataType.INTEGER,pierce);
        boolean slowRam = slowRam();
        if (slowRam)
            container.set(plugin.slowRam,PersistentDataType.BOOLEAN, true);
        boolean gunOffhand = gunOffhand();
        if (!gunOffhand)
            container.set(plugin.gunOffhand,PersistentDataType.BOOLEAN,false);
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
