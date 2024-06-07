package me.stephenminer.customitems.listeners;

import me.stephenminer.customitems.CustomItems;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class TwoHandedListener implements Listener {
    private final CustomItems plugin;
    private final Set<UUID> fatigueSet;
    private int fatigue;
    public TwoHandedListener(){
        this.plugin = JavaPlugin.getPlugin(CustomItems.class);
        fatigueSet = new HashSet<>();
        fatigue = 1;
    }


    @EventHandler
    public void swapHand(PlayerSwapHandItemsEvent event){
        Player player = event.getPlayer();
        ItemStack main = event.getMainHandItem();
        ItemStack off = event.getOffHandItem();
        //Swapping two-handed item from offhand to main hand with an item already in main-hand

        passiveTwoHandCheck(player);
        if (twoHanded(main) && (off != null && !off.getType().isAir())) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING,9999999,fatigue));
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
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING,9999999,fatigue));
            fatigueSet.add(player.getUniqueId());
            player.sendMessage(ChatColor.YELLOW + "You cannot properly wield this weapon with an item in your other hand...");
        }
    }

    @EventHandler
    public void clickHands(InventoryClickEvent event){
        Player player = (Player) event.getWhoClicked();
        // ItemStack mainhand = player.getInventory().getItemInMainHand();

        ItemStack mainhand = player.getInventory().getItemInMainHand();
        ItemStack offhand = player.getInventory().getItemInOffHand();
        ItemStack cursor = event.getCursor();
        passiveTwoHandCheck(player);
        //Case where a twohanded weapon was just placed into the mainhand slot via mouseclick while an item is in the offhand
        if (twoHanded(cursor) && !offhand.getType().isAir() && event.getSlot() == player.getInventory().getHeldItemSlot()){
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING,9999999,fatigue));
            fatigueSet.add(player.getUniqueId());
            player.sendMessage(ChatColor.YELLOW + "You cannot properly wield this weapon with an item in your other hand...");
            return;
        }

        //Case where offhand slot is clicked while a twohanded weapon is in the mainhand
        if (event.getSlotType() == InventoryType.SlotType.QUICKBAR && event.getSlot() == 40){
            if (twoHanded(mainhand)){
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING,9999999,fatigue));
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
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING,9999999,fatigue));
                    fatigueSet.add(player.getUniqueId());
                    player.sendMessage(ChatColor.YELLOW + "You cannot use your other hand while wielding this weapon...");
                }
            }
            if (event.getSlot() > 8 && player.getInventory().firstEmpty() == player.getInventory().getHeldItemSlot()){
                if (!offhand.getType().isAir()){
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING,9999999,fatigue));
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
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING,9999999,fatigue));
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
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING,9999999, fatigue));
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
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING,999999,fatigue));
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
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING,9999999,fatigue));
            fatigueSet.add(player.getUniqueId());
            player.sendMessage(ChatColor.YELLOW + "You cannot use your other hand while wielding this weapon...");
        }
    }


    private boolean twoHanded(ItemStack item){
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().getPersistentDataContainer().has(plugin.twoHanded, PersistentDataType.BOOLEAN))
            return false;
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        return container.get(plugin.twoHanded,PersistentDataType.BOOLEAN);
    }
}
