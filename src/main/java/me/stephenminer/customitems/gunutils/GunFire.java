package me.stephenminer.customitems.gunutils;

import me.stephenminer.customitems.CustomItems;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class GunFire {
    protected final CustomItems plugin;
    protected Player shooter;
    protected double damage, range, decayRange, decayRate;

    public GunFire(Player shooter, double damage, double range, double decayRate, double decayRange){
        this.plugin = JavaPlugin.getPlugin(CustomItems.class);
        this.shooter = shooter;
        this.damage = damage;
        this.range = range;
        this.decayRate = decayRate;
        this.decayRange = decayRange;
    }
    public GunFire(Player shooter, double damage, double range){
        this(shooter, damage, range, 0, 0);
    }



    public void shoot(){
        Location origin = shooter.getEyeLocation();
        Vector dir = origin.getDirection();
        BulletTrace trace = new BulletTrace(shooter, origin, dir,this, true);
        trace.trace();
    }






    public double damage(){ return damage; }
    public double range(){ return range; }
    public double decayRange(){ return decayRange; }
    public double decayRate(){ return decayRate; }

}
