package me.stephenminer.customitems.listeners;

import me.stephenminer.customitems.CustomItems;
import me.stephenminer.customitems.reach.RayTrace;
import me.stephenminer.customitems.reach.ReachAttackHandler;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.RayTraceResult;

import java.util.HashSet;

public class HandleMelee implements Listener {

    private final CustomItems plugin;

    public HandleMelee(){
        this.plugin = JavaPlugin.getPlugin(CustomItems.class);
    }

    @EventHandler
    public void onArmSwing(PlayerAnimationEvent event){
        if (event.getAnimationType() != PlayerAnimationType.ARM_SWING) return;
        ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
        Player player = event.getPlayer();
        if (!hasReach(item)) return;
        int reach = reachTag(item);
        RayTrace ray = new RayTrace(player);
        RayTraceResult result = ray.rayTrace(reach, 0.1d);
        if (result == null) return;
        Entity entity = result.getHitEntity();
        if (entity instanceof LivingEntity living) {
            ReachAttackHandler handler = new ReachAttackHandler(player);
            handler.hitEntity(living);
        }
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent event){
        if (event.getDamager() instanceof Player player && event.getEntity() instanceof LivingEntity living){
            ItemStack item = player.getInventory().getItemInMainHand();
            if (!hasReach(item)) return;
            RayTrace trace = new RayTrace(player);
            RayTraceResult result = trace.rayTrace(2.5, 0.1);
            if (result != null){
                living.setMetadata(ReachAttackHandler.DATA_KEY(player.getUniqueId()),new FixedMetadataValue(plugin,(byte) (0)));
                applyMultipliers(event,mountMultiplier(item));
                return;
            }
            if (living.hasMetadata(ReachAttackHandler.DATA_KEY(player.getUniqueId()))){
                living.removeMetadata(ReachAttackHandler.DATA_KEY(player.getUniqueId()),plugin);
                applyMultipliers(event,mountMultiplier(item));

            }else {
                event.setCancelled(true);
            }
        }
    }


    /**
     *
     * @param event with Player as damager
     */
    private void applyMultipliers(EntityDamageByEntityEvent event, double multiplier){
        Player player = (Player) event.getDamager();
        Entity vehicle = player.getVehicle();
        if (vehicle instanceof LivingEntity && vehicle instanceof Vehicle) {
            event.setDamage(event.getDamage() * multiplier);

        }

    }



    private boolean hasReach(ItemStack item){ return reachTag(item) != -1; }
    /**
     *
     * @param item item to read the tag of
     * @return -1 if item doesn't have reach in persistant data container, however long the reach is otherwise
     */
    private int reachTag(ItemStack item){
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().getPersistentDataContainer().has(plugin.reach, PersistentDataType.INTEGER))
            return -1;
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        return container.get(plugin.reach, PersistentDataType.INTEGER);
    }



    private double mountMultiplier(ItemStack item){
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().getPersistentDataContainer().has(plugin.mounted, PersistentDataType.DOUBLE))
            return 1;
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        return container.get(plugin.mounted,PersistentDataType.DOUBLE);
    }
}
