package me.stephenminer.customitems.reach;

import me.stephenminer.customitems.CustomItems;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

/**
 *
 */
public class ReachAttackHandler {
    public static String DATA_KEY(UUID uuid){
        return "hit:" + uuid.toString();
    }
    private final CustomItems plugin;
    private final Player attacker;

    public ReachAttackHandler(Player attacker){
        this.plugin = JavaPlugin.getPlugin(CustomItems.class);

        this.attacker = attacker;
    }

    public void hitEntity(LivingEntity living, double range){
        if (living.hasMetadata(ReachAttackHandler.DATA_KEY(attacker.getUniqueId()))) {
          //  System.out.println("Damage Denied");
            living.removeMetadata(ReachAttackHandler.DATA_KEY(attacker.getUniqueId()),plugin);
            return;
        }
        ServerPlayer craftPlayer = ((CraftPlayer) attacker).getHandle();
       // living.setMetadata(ReachAttackHandler.DATA_KEY(attacker.getUniqueId()),new FixedMetadataValue(plugin,(byte) (0)));
        craftPlayer.attack(((CraftLivingEntity) living).getHandle());
        if (craftPlayer.distanceToSqr(((CraftLivingEntity)living).getHandle()) >= 9){

        }
    }

    /*
    private void reachMethodCopy(double reach, ServerPlayer player, net.minecraft.world.entity.LivingEntity entity){
        float f4 = 1.0F + EnchantmentHelper.getSweepingDamageRatio(player) * f;
        List<net.minecraft.world.entity.LivingEntity> list = this.level().getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, entity.getBoundingBox().inflate(1.0, 0.25, 1.0));
        Iterator iterator = list.iterator();

        label179:
        while(true) {
            net.minecraft.world.entity.LivingEntity entityliving;
            do {
                do {
                    do {
                        do {
                            if (!iterator.hasNext()) {
                                player.level().playSound((net.minecraft.world.entity.player.Player)null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, this.getSoundSource(), 1.0F, 1.0F);
                                player.sweepAttack();
                                break label179;
                            }

                            entityliving = (net.minecraft.world.entity.LivingEntity)iterator.next();
                        } while(entityliving == player);
                    } while(entityliving == entity);
                } while(player.isAlliedTo(entityliving));
            } while(entityliving instanceof ArmorStand && ((ArmorStand)entityliving).isMarker());

            if (player.distanceToSqr(entityliving) < 9.0 && entityliving.hurt(this.damageSources().playerAttack(this).sweep(), f4)) {
                entityliving.knockback(0.4000000059604645, (double) Mth.sin(this.getYRot() * 0.017453292F), (double)(-Mth.cos(this.getYRot() * 0.017453292F)), this, EntityKnockbackEvent.KnockbackCause.SWEEP_ATTACK);
            }
        }


    }

     */








}
