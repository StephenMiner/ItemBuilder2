package me.stephenminer.customitems.gunutils;

import me.stephenminer.customitems.CustomItems;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;


public class BulletTrace {
    private final CustomItems plugin;
    private final LivingEntity living;
    private final Location origin;
    private final Vector dir;
    private final GunFire host;
    private final boolean iframes;
    private final double boxSize;
    private final Set<EntityType> blacklist;

    /**
     *
     * @param living
     * @param origin
     * @param dir
     * @param gunfire
     */
    public BulletTrace(LivingEntity living, Location origin, Vector dir, GunFire gunfire, boolean iframes){
        this(living,origin,dir,gunfire,iframes, null);
    }
    public BulletTrace(LivingEntity living, Location origin, Vector dir, GunFire gunfire, boolean iframes, Set<EntityType> blacklist){
        boxSize = 0.24;
        this.plugin = JavaPlugin.getPlugin(CustomItems.class);
        this.living = living;
        this.origin = origin;
        this.dir = dir;
        this.host = gunfire;
        this.iframes = iframes;
        this.blacklist = blacklist;
    }


    public LivingEntity trace(){
        Location base = origin.clone();
        World world = living.getWorld();
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
                return null;
            }
            double damage = host.damage();
            if (base.getBlock().isLiquid() && host.waterDecayRate > 0){
                int decayBlocks = Math.max(0, i - (int) host.decayRange);
                double subtractor = host.decayRate * decayBlocks;
                damage -= subtractor;
            }else if (host.decayRange() > 0) {
                int decayBlocks = Math.max(0, i - (int) host.decayRange);
                double subtractor = host.decayRate * decayBlocks;
                damage -= subtractor;
            }
            LivingEntity hit = checkEntityIntersection(base, box);
            if (hit != null){
                if (blacklist != null && blacklist.contains(hit.getType())) continue;
                world.spawnParticle(Particle.BLOCK_CRACK,base, 20,Material.REDSTONE_BLOCK.createBlockData());
                hit.setMetadata("bullet-hit",plugin.bulletHit);
                hit.setMetadata("gunap",new FixedMetadataValue(plugin,host.getIgnoreArmor()));
                damage = checkDamageBonus(hit,damage);
                hit.damage(damage, living);
                if (!iframes) hit.setNoDamageTicks(0);
                return hit;
            }
            base.add(dir);
        }
        return null;
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

    /**
     *
     * @param hit the hit entity
     * @param dmg original damage to be dealt to entity
     * @return dmg + (dmg * damage bonuses)
     */
    private double checkDamageBonus(Entity hit, double dmg){
        if (hit instanceof Player){
            return dmg + (dmg * host.playerBonus);
        }else if (hit instanceof Mob){
            return dmg + (dmg * host.mobBonus);
        }
        return dmg;
    }

    private LivingEntity checkEntityIntersection(Location position, BoundingBox bounds){
        World world = position.getWorld();
        Collection<Entity> inBox = world.getNearbyEntities(bounds, (entity -> entity instanceof LivingEntity livingEntity && !livingEntity.equals(living) && (blacklist == null || !blacklist.contains(livingEntity.getType()))));
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


    public void setIgnoreArmor(float ignoreArmor){

    }

}
