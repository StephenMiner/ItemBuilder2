package me.stephenminer.customitems.gunutils;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class SpreadGunFire extends GunFire{
    private int projectiles;
    private double spread;
    public SpreadGunFire(LivingEntity shooter, double damage, double range, double decayRate, double decayRange, int projectiles, Set<EntityType> blacklist) {
        super(shooter, damage, range, decayRate, decayRange, blacklist);
        this.projectiles = projectiles;
        spread = 0.25;
    }
    public SpreadGunFire(LivingEntity shooter, double damage, double range, double decayRate, double decayRange, int projectiles) {
        super(shooter, damage, range, decayRate, decayRange);
        this.projectiles = projectiles;
        spread = 0.25;
    }



    @Override
    public void shoot(){
        Random random = new Random();
        Location origin = shooter.getEyeLocation();
        Vector baseDir = shooter.getEyeLocation().getDirection();
        boolean hit = false;
        List<LivingEntity> hits = new ArrayList<>();
        for (int i = 0; i < projectiles; i++){
            Vector dir = baseDir.clone()
                    .add(new Vector(random.nextDouble()*spread, random.nextDouble()*spread, random.nextDouble()*spread))
                    .subtract(new Vector(random.nextDouble() *spread, random.nextDouble()*spread, random.nextDouble() * spread))
                    .normalize();
            BulletTrace trace = new BulletTrace(shooter,origin,dir,this,false, blacklist);
            LivingEntity traced = trace.trace();
            if (traced != null) {
                hit = true;
                hits.add(traced);
            }
        }
        hits.forEach(living->living.setNoDamageTicks(2));
        if (hit && shooter instanceof Player player)
            player.playSound(shooter, Sound.ENTITY_ARROW_HIT_PLAYER,1,1);
    }

    public void setSpread(double spread){ this.spread = spread; }
    public double getSpread(){ return spread; }
}
