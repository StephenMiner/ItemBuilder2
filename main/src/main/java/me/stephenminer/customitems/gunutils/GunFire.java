package me.stephenminer.customitems.gunutils;

import me.stephenminer.customitems.CustomItems;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import java.util.Set;

public class GunFire {
    protected final CustomItems plugin;
    protected LivingEntity shooter;
    protected double damage, range, decayRange, decayRate, waterDecayRate, size;
    protected final Set<EntityType> blacklist;
    //A modifier from 0-1.0 where 0 represents 0% damage ignored due to armor, and 1 representing 100%
    protected float ignoreArmor, playerBonus, mobBonus;

    public GunFire(LivingEntity shooter, double damage, double range, double decayRate, double decayRange, Set<EntityType> blacklist){
        this(shooter,damage,range,decayRate,decayRange,decayRate,blacklist);
    }
    public GunFire(LivingEntity shooter, double damage, double range, double decayRate, double decayRange, double waterdecayRate, Set<EntityType> blacklist){
        this.plugin = JavaPlugin.getPlugin(CustomItems.class);
        this.shooter = shooter;
        this.damage = damage;
        this.range = range;
        this.decayRate = decayRate;
        this.decayRange = decayRange;
        this.blacklist = blacklist;
        this.waterDecayRate = waterdecayRate;
        ignoreArmor = 1.0f;
    }
    public GunFire(LivingEntity shooter, double damage, double range, double decayRate, double decayRange){
        this(shooter,damage,range,decayRate,decayRange, null);
    }

    public GunFire(LivingEntity shooter, double damage, double range){
        this(shooter, damage, range, 0, 0);
    }



    public void shoot(){
        Location origin = shooter.getEyeLocation();
        Vector dir = origin.getDirection();
        BulletTrace trace = new BulletTrace(shooter, origin, dir,this, true, blacklist,size);
        boolean hit = trace.trace() != null;
        if (hit && shooter instanceof Player player){
            player.playSound(shooter, Sound.ENTITY_ARROW_HIT_PLAYER,1,1);
        }
    }






    public double damage(){ return damage; }
    public double range(){ return range; }
    public double decayRange(){ return decayRange; }
    public double waterDecayRate(){ return waterDecayRate; }
    public double decayRate(){ return decayRate; }
    public double size(){ return size; }
    /**
     *
     * @param ignoreArmor A float from 0-1.0 where 0 represents 0% damage ignored due to armor, and 1 representing 100%
     */
    public void setIgnoreArmor(float ignoreArmor){
        this.ignoreArmor = ignoreArmor;
    }

    /**
     *
     * @return A float from 0-1.0 where 0 represents 0% damage ignored due to armor, and 1 representing 100%
     */
    public float getIgnoreArmor(){
        return ignoreArmor;
    }

    public void setWaterDecayRate(double waterDecayRate){ this.waterDecayRate = waterDecayRate; }

    public void setMobBonus(float mobBonus){ this.mobBonus = mobBonus; }
    public void setPlayerBonus(float playerBonus) { this.playerBonus = playerBonus; }

    public float getPlayerBonus(){ return playerBonus; }
    public float getMobBonus(){ return mobBonus; }

    public void setBoxSize(double size){ this.size = size;}
}
