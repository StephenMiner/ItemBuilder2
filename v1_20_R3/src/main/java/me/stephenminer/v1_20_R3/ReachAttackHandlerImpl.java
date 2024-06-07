package me.stephenminer.v1_20_R3;

import me.stephenminer.customitems.reach.ReachAttackHandler;
import me.stephenminer.customitems.CustomItems;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;


public class ReachAttackHandlerImpl implements ReachAttackHandler {
    private final CustomItems plugin;
    private final Player attacker;

    public ReachAttackHandlerImpl(Player player){
        this.attacker = player;
        this.plugin = JavaPlugin.getPlugin(CustomItems.class);
    }
    @Override
    public void hitEntity(LivingEntity living) {
        if (living.hasMetadata(ReachAttackHandler.DATA_KEY(attacker.getUniqueId()))) {
            //  System.out.println("Damage Denied");
            living.removeMetadata(ReachAttackHandler.DATA_KEY(attacker.getUniqueId()),plugin);
            return;
        }

        ServerPlayer craftPlayer = ((CraftPlayer) attacker).getHandle();
        // living.setMetadata(ReachAttackHandler.DATA_KEY(attacker.getUniqueId()),new FixedMetadataValue(plugin,(byte) (0)));
        craftPlayer.attack(((CraftLivingEntity) living).getHandle());
    }
}