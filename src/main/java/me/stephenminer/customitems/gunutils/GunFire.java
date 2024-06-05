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
    protected double damage, range, decayRange, decayRate;
    protected final Set<EntityType> blacklist;

    public GunFire(LivingEntity shooter, double damage, double range, double decayRate, double decayRange, Set<EntityType> blacklist){
        this.plugin = JavaPlugin.getPlugin(CustomItems.class);
        this.shooter = shooter;
        this.damage = damage;
        this.range = range;
        this.decayRate = decayRate;
        this.decayRange = decayRange;
        this.blacklist = blacklist;
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
        BulletTrace trace = new BulletTrace(shooter, origin, dir,this, true, blacklist);
        boolean hit = trace.trace() != null;
        if (hit && shooter instanceof Player player){
            player.playSound(shooter, Sound.ENTITY_ARROW_HIT_PLAYER,1,1);
        }
    }






    public double damage(){ return damage; }
    public double range(){ return range; }
    public double decayRange(){ return decayRange; }
    public double decayRate(){ return decayRate; }

}
