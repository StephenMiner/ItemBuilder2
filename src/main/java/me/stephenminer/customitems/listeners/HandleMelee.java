package me.stephenminer.customitems.listeners;

import me.stephenminer.customitems.CustomItems;
import me.stephenminer.customitems.reach.RayTrace;
import me.stephenminer.customitems.reach.ReachAttackHandler;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.RayTraceResult;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class HandleMelee implements Listener {
    public static NamespacedKey REACH = new NamespacedKey(JavaPlugin.getPlugin(CustomItems.class),"reach");
    public static NamespacedKey TWO_HANDED = new NamespacedKey(JavaPlugin.getPlugin(CustomItems.class),"twohanded");
    public static NamespacedKey MOUNTED = new NamespacedKey(JavaPlugin.getPlugin(CustomItems.class),"mounted");
    private final CustomItems plugin;

    private Set<UUID> fatigueSet;

    public HandleMelee(){
        this.plugin = JavaPlugin.getPlugin(CustomItems.class);
        fatigueSet = new HashSet<>();
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
            System.out.println(99);
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
                System.out.println("Damage Allowed");
                System.out.println(event.getDamage());

            }else {
                event.setCancelled(true);
                System.out.println("Damage Stopped");
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
            System.out.println("Multiplier-Applied");
        }

    }


    @EventHandler
    public void swapHand(PlayerSwapHandItemsEvent event){
        Player player = event.getPlayer();
        ItemStack main = event.getMainHandItem();
        ItemStack off = event.getOffHandItem();
        //Swapping two-handed item from offhand to main hand with an item already in main-hand
        System.out.println(twoHanded(main));
        passiveTwoHandCheck(player);
        if (twoHanded(main) && (off != null && !off.getType().isAir())) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING,9999999,9));
            fatigueSet.add(player.getUniqueId());
            player.sendMessage(ChatColor.YELLOW + "You cannot properly wield this weapon with an item in your other hand...");
        }
    }

    @EventHandler
    public void selectItem(PlayerItemHeldEvent event){
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItem(event.getNewSlot());
        ItemStack offhand = player.getInventory().getItemInOffHand();
        passiveTwoHandCheck(player);
        if (twoHanded(item) && !offhand.getType().isAir()){
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING,9999999,9));
            fatigueSet.add(player.getUniqueId());
            player.sendMessage(ChatColor.YELLOW + "You cannot properly wield this weapon with an item in your other hand...");
        }
    }

    @EventHandler
    public void clickHands(InventoryClickEvent event){
        ItemStack item = event.getCurrentItem();
        if (item != null && item.hasItemMeta()){
            Collection<AttributeModifier> mods = item.getItemMeta().getAttributeModifiers(Attribute.GENERIC_ATTACK_SPEED);
            if (mods != null) {
                mods.forEach(mod -> {
                    System.out.println(mod.getAmount());
                });
            }
        }
        Player player = (Player) event.getWhoClicked();
       // ItemStack mainhand = player.getInventory().getItemInMainHand();

        ItemStack mainhand = player.getInventory().getItemInMainHand();
        ItemStack offhand = player.getInventory().getItemInOffHand();
        ItemStack cursor = event.getCursor();
        passiveTwoHandCheck(player);
        //Case where a twohanded weapon was just placed into the mainhand slot via mouseclick while an item is in the offhand
        if (twoHanded(cursor) && !offhand.getType().isAir() && event.getSlot() == player.getInventory().getHeldItemSlot()){
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING,9999999,9));
            fatigueSet.add(player.getUniqueId());
            player.sendMessage(ChatColor.YELLOW + "You cannot properly wield this weapon with an item in your other hand...");
            return;
        }

        //Case where offhand slot is clicked while a twohanded weapon is in the mainhand
        if (event.getSlotType() == InventoryType.SlotType.QUICKBAR && event.getSlot() == 40){
            if (twoHanded(mainhand)){
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING,9999999,9));
                fatigueSet.add(player.getUniqueId());
                player.sendMessage(ChatColor.YELLOW + "You cannot use your other hand while wielding this weapon...");
                return;
            }
        }

        if (event.getClick() == ClickType.NUMBER_KEY || event.getClick() == ClickType.SHIFT_LEFT){
            ItemStack current = event.getCurrentItem();
            if (!twoHanded(current)) return;
            if (event.getHotbarButton() == player.getInventory().getHeldItemSlot()){
                if (!offhand.getType().isAir()){
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING,9999999,9));
                    fatigueSet.add(player.getUniqueId());
                    player.sendMessage(ChatColor.YELLOW + "You cannot use your other hand while wielding this weapon...");
                }
            }
            if (event.getSlot() > 8 && player.getInventory().firstEmpty() == player.getInventory().getHeldItemSlot()){
                if (!offhand.getType().isAir()){
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING,9999999,9));
                    fatigueSet.add(player.getUniqueId());
                    player.sendMessage(ChatColor.YELLOW + "You cannot use your other hand while wielding this weapon...");
                }
            }
        }

    }

    @EventHandler
    public void inv(InventoryDragEvent event){
        Player player = (Player) event.getWhoClicked();
        ItemStack hand = player.getInventory().getItemInMainHand();
        passiveTwoHandCheck(player);
        //Case where items are dragged into offhand while twohanded sword is in hand
        if (twoHanded(hand)){

            if (event.getInventorySlots().contains(40)) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING,9999999,9));
                fatigueSet.add(player.getUniqueId());
                player.sendMessage(ChatColor.YELLOW + "You cannot use your other hand while wielding this weapon...");
                return;
            }
        }

        //Case where twohanded weapon is "dragged" into main hand slot while there are items in offhand
        //This one doesn't really make sense, but it does apply sometimes since you can technically drag items of 1 stack
        if (event.getInventorySlots().contains(player.getInventory().getHeldItemSlot())){
            ItemStack offhand = player.getInventory().getItemInOffHand();
            if (twoHanded(event.getOldCursor()) && !offhand.getType().isAir()){
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING,9999999,9));
                fatigueSet.add(player.getUniqueId());
                player.sendMessage(ChatColor.YELLOW + "You cannot use your other hand while wielding this weapon...");
                return;
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        ItemStack main = player.getInventory().getItemInMainHand();
        ItemStack off = player.getInventory().getItemInOffHand();
        if (twoHanded(main) && !off.getType().isAir()){
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING,999999,9));
            fatigueSet.add(player.getUniqueId());
            player.sendMessage(ChatColor.YELLOW + "You cannot use your other hand while wielding this weapon...");
        }
    }

    /**
     *
     * @param player
     */
    private void passiveTwoHandCheck(Player player){
        Bukkit.getScheduler().runTaskLater(plugin,()->{
            ItemStack main = player.getInventory().getItemInMainHand();
            ItemStack off = player.getInventory().getItemInOffHand();
            if (!(twoHanded(main) && !off.getType().isAir())){
                fatigueSet.remove(player.getUniqueId());
                player.removePotionEffect(PotionEffectType.SLOW_DIGGING);
            }
        } ,1);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event){
        Player player = event.getPlayer();
        if (fatigueSet.contains(player.getUniqueId())){
            player.removePotionEffect(PotionEffectType.SLOW_DIGGING);
            fatigueSet.remove(player.getUniqueId());
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event){
        Player player = (Player) event.getPlayer();
        ItemStack cursor = player.getItemOnCursor();
        ItemStack off = player.getInventory().getItemInOffHand();
        if (twoHanded(cursor) && !off.getType().isAir() && player.getInventory().firstEmpty() == player.getInventory().getHeldItemSlot()){
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING,9999999,9));
            fatigueSet.add(player.getUniqueId());
            player.sendMessage(ChatColor.YELLOW + "You cannot use your other hand while wielding this weapon...");
        }
    }

    private boolean hasReach(ItemStack item){ return reachTag(item) != -1; }
    /**
     *
     * @param item item to read the tag of
     * @return -1 if item doesn't have reach in persistant data container, however long the reach is otherwise
     */
    private int reachTag(ItemStack item){
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().getPersistentDataContainer().has(REACH, PersistentDataType.INTEGER))
            return -1;
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        return container.get(HandleMelee.REACH, PersistentDataType.INTEGER);
    }

    private boolean twoHanded(ItemStack item){
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().getPersistentDataContainer().has(TWO_HANDED,PersistentDataType.BOOLEAN))
            return false;
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        return container.get(TWO_HANDED,PersistentDataType.BOOLEAN);
    }

    private double mountMultiplier(ItemStack item){
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().getPersistentDataContainer().has(MOUNTED, PersistentDataType.DOUBLE))
            return 1;
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        return container.get(MOUNTED,PersistentDataType.DOUBLE);
    }
}
