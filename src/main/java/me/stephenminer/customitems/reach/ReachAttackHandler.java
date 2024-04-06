package me.stephenminer.customitems.reach;

import me.stephenminer.customitems.CustomItems;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
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

    public void hitEntity(LivingEntity living){
        if (living.hasMetadata(ReachAttackHandler.DATA_KEY(attacker.getUniqueId()))) {
            System.out.println("Damage Denied");
            living.removeMetadata(ReachAttackHandler.DATA_KEY(attacker.getUniqueId()),plugin);
            return;
        }
        ServerPlayer craftPlayer = ((CraftPlayer) attacker).getHandle();
        living.setMetadata(ReachAttackHandler.DATA_KEY(attacker.getUniqueId()),new FixedMetadataValue(plugin,(byte) (0)));
        craftPlayer.attack(((CraftLivingEntity) living).getHandle());
    }








}
