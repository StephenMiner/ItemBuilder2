package me.stephenminer.customitems.gunutils;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Random;

public class SpreadGunFire extends GunFire{
    private int projectiles;
    public SpreadGunFire(Player shooter, double damage, double range, double decayRate, double decayRange, int projectiles) {
        super(shooter, damage, range, decayRate, decayRange);
        this.projectiles = projectiles;
    }



    @Override
    public void shoot(){
        Random random = new Random();
        Location origin = shooter.getEyeLocation();
        Vector baseDir = shooter.getEyeLocation().getDirection();
        boolean hit = false;
        for (int i = 0; i < projectiles; i++){
            Vector dir = baseDir.clone()
                    .add(new Vector(random.nextDouble()*0.25, random.nextDouble()*0.25, random.nextDouble()*0.25))
                    .subtract(new Vector(random.nextDouble() *0.25, random.nextDouble()*0.25, random.nextDouble() * 0.25))
                    .normalize();
            BulletTrace trace = new BulletTrace(shooter,origin,dir,this,false);
            if (trace.trace()) hit = true;
        }
        if (hit) shooter.playSound(shooter, Sound.ENTITY_ARROW_HIT_PLAYER,1,1);
    }
}
