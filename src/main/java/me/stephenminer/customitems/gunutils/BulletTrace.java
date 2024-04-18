package me.stephenminer.customitems.gunutils;

import me.stephenminer.customitems.CustomItems;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;


public class BulletTrace {
    private final CustomItems plugin;
    private final Player shooter;
    private final Location origin;
    private final Vector dir;
    private final GunFire host;
    private final boolean iframes;
    private final double boxSize;

    /**
     *
     * @param player
     * @param origin
     * @param dir
     * @param gunfire
     */
    public BulletTrace(Player player, Location origin, Vector dir, GunFire gunfire, boolean iframes){
        boxSize = 0.15;
        this.plugin = JavaPlugin.getPlugin(CustomItems.class);
        this.shooter = player;
        this.origin = origin;
        this.dir = dir;
        this.host = gunfire;
        this.iframes = iframes;
    }


    public boolean trace(){
        Location base = origin.clone();
        World world = shooter.getWorld();
        //world.spawnParticle(Particle.CAMPFIRE_SIGNAL_SMOKE,base.clone().add(dir),1);
        for (int i = 0; i < 2; i++)
            world.spawnParticle(Particle.CAMPFIRE_COSY_SMOKE,base.clone().add(dir),ThreadLocalRandom.current().nextInt(1));
        for (int i = 0; i <= (int)host.range; i++){
            world.spawnParticle(Particle.ASH,base,4);
            Vector min = base.clone().subtract(boxSize, boxSize, boxSize).toVector();
            Vector max = base.clone().add(boxSize, boxSize,boxSize).toVector();
            BoundingBox box = BoundingBox.of(min,max);
            Block intersecting = checkBlockIntersection(base,box);
            if (intersecting != null){
                world.spawnParticle(Particle.BLOCK_CRACK,base,20, intersecting.getType().createBlockData());
                SoundGroup group = intersecting.getBlockData().getSoundGroup();
                world.playSound(base, group.getBreakSound(),group.getVolume(),group.getPitch());
                return false;
            }
            LivingEntity hit = checkEntityIntersection(base, box);
            if (hit != null){
                double damage = host.damage();
                world.spawnParticle(Particle.BLOCK_CRACK,base, 20,Material.REDSTONE_BLOCK.createBlockData());
                if (host.decayRange() > 0) {
                    int decayBlocks = Math.max(0, i - (int) host.decayRange);
                    double subtractor = host.decayRate * decayBlocks;
                    damage -= subtractor;
                }

                hit.damage(damage,shooter);
                if (!iframes) hit.setNoDamageTicks(0);
                return true;
            }
            base.add(dir);
        }
        return false;
    }

    private Block checkBlockIntersection(Location position, BoundingBox bounds){
        Block block = position.getBlock();
        World world = position.getWorld();
        if (!block.isPassable()){
            if (block.getBoundingBox().overlaps(bounds)) {
                return block;
            }
        }
        return null;
    }

    private LivingEntity checkEntityIntersection(Location position, BoundingBox bounds){
        World world = position.getWorld();
        Collection<Entity> inBox = world.getNearbyEntities(bounds, (entity -> entity instanceof LivingEntity && !shooter.equals(entity)));
        if (inBox.size() < 1) return null;
        int rand = ThreadLocalRandom.current().nextInt(inBox.size());
        int count = 0;
        Entity selection = null;
        for (Entity entity : inBox){
            if (count == rand){
                selection = entity;
                break;
            }
            count++;
        }
        return (LivingEntity) selection;
    }


}
