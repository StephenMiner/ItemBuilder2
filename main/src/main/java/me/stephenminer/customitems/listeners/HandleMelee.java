package me.stephenminer.customitems.listeners;

import me.stephenminer.customitems.CustomItems;
import me.stephenminer.customitems.reach.RayTrace;
import me.stephenminer.customitems.reach.ReachAttackHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.RayTraceResult;

public class HandleMelee implements Listener {

    private final CustomItems plugin;

    public HandleMelee(){
        this.plugin = JavaPlugin.getPlugin(CustomItems.class);
    }

    @EventHandler
    public void onArmSwing(PlayerAnimationEvent event){

        if (event.getAnimationType() != PlayerAnimationType.ARM_SWING) return;
        ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
        if (!item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();
        Player player = event.getPlayer();

        if (!hasReach(meta)) return;
        int reach = reachTag(meta);
        RayTrace ray = new RayTrace(player);
        RayTraceResult result = ray.rayTrace(reach, 0.1d);
        if (result == null) return;
        Entity entity = result.getHitEntity();
        if (entity instanceof LivingEntity living) {
            ReachAttackHandler handler = reflectClass(player);
            if (handler == null) return;
            handler.hitEntity(living);
           // ReachAttackHandler handler = new ReachAttackHandlerImpl(player);
         //   handler.hitEntity(living);
        }

    }

    private ReachAttackHandler reflectClass(Player player){
        ReachAttackHandler out;
        try{
            String packageName = "me.stephenminer";
            String name = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
            out = (ReachAttackHandler) Class.forName(packageName +"." +  name + ".ReachAttackHandlerImpl").getConstructor(Player.class).newInstance(player);
            return out;
        }catch (Exception e){ e.printStackTrace();}
        return null;
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent event){
        if (event.getDamager() instanceof Player player && event.getEntity() instanceof LivingEntity living){
            if (living.hasMetadata("bullet-hit")) return;
            ItemStack item = player.getInventory().getItemInMainHand();
            if (!item.hasItemMeta()) return;
            ItemMeta meta = item.getItemMeta();
            if (!hasReach(meta)) {
                applyMultipliers(event, meta);
                return;
            }
            RayTrace trace = new RayTrace(player);
            RayTraceResult result = trace.rayTrace(3.5, 0.1);
            if (result != null){
                double trueRange = reachTag(meta);
                if (trueRange < 3.5){
                    result = trace.rayTrace(trueRange,0.1);
                    if (result == null){
                        event.setCancelled(true);
                        return;
                    }
                }
                living.setMetadata(ReachAttackHandler.DATA_KEY(player.getUniqueId()),new FixedMetadataValue(plugin,(byte) (0)));
                applyMultipliers(event, meta);
             //   System.out.println("normal damage");
              //  System.out.println(event.getDamage());
                return;
            }else{
                living.removeMetadata(ReachAttackHandler.DATA_KEY(player.getUniqueId()),plugin);
                applyMultipliers(event, meta);
            //    System.out.println("reach damage");
            }
            /*
            if (living.hasMetadata(ReachAttackHandler.DATA_KEY(player.getUniqueId()))){


             //   System.out.println(event.getDamage());

            }else {
                event.setCancelled(true);
            }

             */
        }
    }


    /**
     * Will apply all damage modifiers
     * @param event with Player as damager
     * @param meta ItemMeta to read multipliers from
     */
    private void applyMultipliers(EntityDamageByEntityEvent event, ItemMeta meta){
        if (!applyBonus(meta))return;
        Entity damager = event.getDamager();
        Entity vehicle = damager.getVehicle();
        Entity attacked = event.getEntity();
        double damage = event.getDamage();
        if (vehicle instanceof LivingEntity) {
            damage *= mountMultiplier(meta);
        }
        if (attacked instanceof Player)
            damage += (damage * playerBonus(meta));
        else if (attacked instanceof Mob)
            damage += (damage * mobBonus(meta));
        event.setDamage(damage);
        applyArmorIgnore(event, meta);
    }

    /**
     * Applies melee armor piercing values to melee EntityDamageByEntityEvent values
     * @param event event to grab and set data from and to
     * @param meta ItemMeta to read data from
     */
    private void applyArmorIgnore(EntityDamageByEntityEvent event, ItemMeta meta){
        float ignoreArmor = ignoreArmor(meta);
        if (ignoreArmor == 1) return;
        double armor = event.getDamage(EntityDamageEvent.DamageModifier.ARMOR);
        event.setDamage(EntityDamageEvent.DamageModifier.ARMOR, armor * ignoreArmor);
    }



    private boolean hasReach(ItemMeta meta){ return reachTag(meta) != -1; }
    /**
     *
     * @param meta itemmeta to read the tag from
     * @return -1 if item doesn't have reach in persistant data container, however long the reach is otherwise
     */
    private int reachTag(ItemMeta meta){
        if (meta == null || !meta.getPersistentDataContainer().has(plugin.reach, PersistentDataType.INTEGER))
            return -1;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        return container.get(plugin.reach, PersistentDataType.INTEGER);
    }

    private float playerBonus(ItemMeta meta){
        if (meta == null) return 0;
        else return meta.getPersistentDataContainer().getOrDefault(plugin.playerBonus,PersistentDataType.FLOAT,0f);
    }
    private float mobBonus(ItemMeta meta){
        if (meta == null) return 0;
        else return meta.getPersistentDataContainer().getOrDefault(plugin.mobBonus,PersistentDataType.FLOAT,0f);
    }



    private double mountMultiplier(ItemMeta meta){
        if (meta == null || !meta.getPersistentDataContainer().has(plugin.mounted, PersistentDataType.DOUBLE))
            return 1;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        return container.get(plugin.mounted,PersistentDataType.DOUBLE);
    }

    private boolean isGun(ItemMeta meta){
        return meta.getPersistentDataContainer().has(plugin.gun, PersistentDataType.STRING);
    }

    private float ignoreArmor(ItemMeta meta) {
        return meta.getPersistentDataContainer().getOrDefault(plugin.ignoreArmor,PersistentDataType.FLOAT,1f);
    }

    /**
     * Defines whether damage bonuses (mount multiplier, dmg v player dmg v mobs, armor-piercing) should be applied or not
     * @param meta the ItemMeta to read data from
     * @return true if the item isnt a gun or if the item is a gun and has the rangedmelee tag
     */
    private boolean applyBonus(ItemMeta meta){
        return !isGun(meta) || meta.getPersistentDataContainer().has(plugin.rangedMelee,PersistentDataType.BOOLEAN);
    }
}
