package me.stephenminer.customitems.listeners;

import me.stephenminer.customitems.CustomItems;
import net.minecraft.world.item.ShieldItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ShieldListener implements Listener {
    private final CustomItems plugin;
    public HashMap<UUID, ItemStack[]> backupItems;

    public ShieldListener(){
        this.plugin = JavaPlugin.getPlugin(CustomItems.class);
        backupItems = new HashMap<>();
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent event){
        if (event.getEntity() instanceof Player player && event.getDamager() instanceof LivingEntity living){
            if (!player.isHandRaised()) return;
            ItemStack active = player.getItemInUse();

            if (active == null || active.getType() != Material.SHIELD) {
                return;
            }
            if (living instanceof Player damager){
                //TODO: Potential check for if weapon is fully charged
            }
            EntityEquipment equipment = living.getEquipment();
            if (equipment != null){
                int breakerTicks = breakerTag(equipment.getItemInMainHand());
                if (breakerTicks >= 0){
                 //   player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 999999,254));
                    player.setCooldown(Material.SHIELD, breakerTicks);
                    player.getWorld().playSound(player.getLocation(), Sound.ITEM_SHIELD_BREAK,1,1);
                    shieldWorkaround(player);
                }
            }
        }
    }

    @EventHandler
    public void attemptShield(PlayerInteractEvent event){
        if (!event.hasItem()) return;
        ItemStack item = event.getItem();
        if (Material.SHIELD == item.getType()){
            if (event.getPlayer().hasCooldown(Material.SHIELD)){
                event.setUseItemInHand(Event.Result.DENY);
                event.setCancelled(true);
            }
        }
    }

    private void shieldWorkaround(Player player){
        ItemStack mainhand = player.getInventory().getItemInMainHand();
        ItemStack dummy = dummy();
        ItemStack[] save = new ItemStack[2];
        save[0] = mainhand;
        if (mainhand.getType() == Material.SHIELD){
            player.getInventory().setItemInMainHand(dummy);
            Bukkit.getScheduler().runTaskLater(plugin,()->{
                backupItems.remove(player.getUniqueId());
                if (!player.isDead()) {
                    int slot = slot(dummy, player.getInventory());
                    player.getInventory().setItem(slot,mainhand);
                }
            }, 2);
        }

        ItemStack offhand = player.getInventory().getItemInOffHand();
        save[1] = offhand;
        backupItems.put(player.getUniqueId(),save);
        if (offhand.getType() == Material.SHIELD){
            player.getInventory().setItemInOffHand(dummy);
            Bukkit.getScheduler().runTaskLater(plugin,()->{
                if (!player.isDead()){
                    int slot = slot(dummy, player.getInventory());
                    player.getInventory().setItem(slot,offhand);
                }
                backupItems.remove(player.getUniqueId());
            },2);
        }

    }


    private int slot(ItemStack item, Inventory inv){
        for (int i = 0; i < inv.getSize(); i++){
            if (item.isSimilar(inv.getItem(i))) return i;
        }
        return -1;
    }
    @EventHandler
    public void noDropDummy(PlayerDropItemEvent event){
        ItemStack item = event.getItemDrop().getItemStack();;
        if (!item.hasItemMeta() || !item.getItemMeta().getPersistentDataContainer().has(plugin.dummy, PersistentDataType.BOOLEAN))
            return;
        event.setCancelled(true);
    }


    private ItemStack dummy(){
        ItemStack item = new ItemStack(Material.STICK);
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(plugin.dummy,PersistentDataType.BOOLEAN,true);
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void quit(PlayerQuitEvent event){
        Player player = event.getPlayer();
        if (!backupItems.containsKey(player.getUniqueId())) return;
        ItemStack[] save = backupItems.get(player.getUniqueId());
        player.getInventory().setItemInMainHand(save[0]);
        player.getInventory().setItemInOffHand(save[1]);
    }


    private int breakerTag(ItemStack item){
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().getPersistentDataContainer().has(plugin.shieldbreaker, PersistentDataType.INTEGER))
            return -1;
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        return container.get(plugin.shieldbreaker, PersistentDataType.INTEGER);
    }
}
