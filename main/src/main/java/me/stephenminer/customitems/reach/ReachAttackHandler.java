package me.stephenminer.customitems.reach;

import org.bukkit.entity.LivingEntity;

import java.util.UUID;

/**
 *
 */
public interface ReachAttackHandler {
    public static String DATA_KEY(UUID uuid){
        return "hit:" + uuid.toString();
    }



    public void hitEntity(LivingEntity living);










}
