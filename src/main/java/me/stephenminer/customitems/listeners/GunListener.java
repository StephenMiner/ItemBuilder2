package me.stephenminer.customitems.listeners;

import me.stephenminer.customitems.CustomItems;
import me.stephenminer.customitems.builder.GunBuilder;
import me.stephenminer.customitems.gunutils.GunFire;
import me.stephenminer.customitems.gunutils.GunReader;
import me.stephenminer.customitems.gunutils.SpreadGunFire;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.CrossbowMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class GunListener implements Listener {
    private Set<UUID> openInv,dropped, ramming;
    private final CustomItems plugin;
    public GunListener(){
        this.plugin = JavaPlugin.getPlugin(CustomItems.class);
        openInv = new HashSet<>();
        dropped = new HashSet<>();
        ramming = new HashSet<>();

    }

    /*
    @EventHandler (ignoreCancelled = true)
    public void attemptFire(EntityShootBowEvent event){
        ItemStack bow = event.getBow();
        if (event.getEntity() instanceof Player player) {
            if (bow == null || bow.getType() == Material.AIR || !bow.hasItemMeta()) return;
            ItemMeta meta = bow.getItemMeta();
            if (meta instanceof CrossbowMeta crossbowMeta) {
                if (isGun(crossbowMeta)) {
                    event.setCancelled(true);
                    event.setConsumeItem(false);
                    System.out.println(1);
                    GunReader reader = new GunReader(bow, crossbowMeta);
                    if (reader.getFiringStage().equals("ready to fire")) {
                        fire(player,reader);
                        System.out.println(2);
                    }
                }
            }
        }
    }

     */



    @EventHandler
    public void loading(PlayerSwapHandItemsEvent event){
        ItemStack offhand = event.getMainHandItem();
        ItemStack mainhand = event.getOffHandItem();
        if (mainhand == null || mainhand.getType().isAir() || !mainhand.hasItemMeta()) return;
        ItemMeta meta = mainhand.getItemMeta();
        if (isGun(meta)){
            event.setCancelled(true);
            Player player = event.getPlayer();
            if (player.hasCooldown(mainhand.getType())){
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("Gun is on cooldown"));
                return;
            }
            GunReader reader = new GunReader(player.getInventory().getItem(EquipmentSlot.HAND), meta);
            String stage = reader.getFiringStage();

            switch (stage) {
                case "prepare powder" ->{
                    loadPowder(reader, player, player.getInventory().getItem(EquipmentSlot.OFF_HAND));
                   }
                case "prepare ammo" -> {
                    loadShot(reader, player, player.getInventory().getItem(EquipmentSlot.OFF_HAND));
                }
                case "ramming" -> {
                    if (ramming.contains(player.getUniqueId())) return;
                    ramming(reader, player, player.getInventory().getItem(EquipmentSlot.OFF_HAND));
                }
            }
        }
    }

    @EventHandler
    public void preventDamaging(PlayerItemDamageEvent event){
        ItemStack item = event.getItem();
        if (!item.hasItemMeta() || item.getType().isAir()) return;
        ItemMeta meta = item.getItemMeta();
        if (isGun(meta)){
            event.setCancelled(true);
        }
    }
    @EventHandler (priority = EventPriority.LOWEST)
    public void preventGunInteraction(PlayerInteractEvent event){
        if (!event.hasItem()) return;
        ItemStack bow = event.getItem();
        Player player = event.getPlayer();

        ItemMeta meta = bow.getItemMeta();
        if (bow.getType() == Material.AIR || !bow.hasItemMeta()) return;
        if (!isGun(meta)) return;
        event.setCancelled(true);
        if (player.hasCooldown(bow.getType())) return;
        Action action = event.getAction();
        if (meta instanceof CrossbowMeta crossbowMeta && (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK)) {
            GunReader reader = new GunReader(bow, crossbowMeta);
            if (reader.getFiringStage().equals("ready to fire")) {
                fire(player,reader);
            }
        }

    }


    private void loadPowder(GunReader reader, Player player, ItemStack offhand){
        if (offhand == null || offhand.getType().isAir() || !offhand.hasItemMeta()) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("You need an appropriate powder in your offhand to begin loading"));
            return;
        }
        if (reader.validPowder(offhand)) {
            offhand.setAmount(offhand.getAmount() - 1);
            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_SAND_PLACE, 2, 1);
            reader.setFiringStage();
            reader.updateDurability();
            if (!checkReadyFire(player,reader))
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR,new TextComponent("Loaded Powder"));
        } else player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("You need an appropriate powder in your offhand to begin loading"));
    }

    private void loadShot(GunReader reader, Player player, ItemStack offhand){
        if (offhand == null || offhand.getType().isAir() || !offhand.hasItemMeta()) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("You need the appropriate ammo in your offhand to begin loading"));
            return;
        }
        if (reader.validAmmo(offhand)) {
            offhand.setAmount(offhand.getAmount() - 1);
            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_DISPENSER_FAIL, 1, 2);
            reader.setFiringStage();
            reader.updateDurability();
            if (!checkReadyFire(player,reader))
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR,new TextComponent("Loaded Shot"));
        } else
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("You need the appropiate ammo in your offhand to begin loading"));
    }

    private void ramming(GunReader reader, Player player, ItemStack offhand){
        PlayerInventory inv = player.getInventory();
        int slot = inv.getHeldItemSlot();
        UUID uuid = player.getUniqueId();
        ItemStack mainhand = inv.getItem(EquipmentSlot.HAND);
        int max = reader.readRamTime();
        Damageable damageable = (Damageable) reader.getItemMeta();
        damageable.setDamage(reader.getHost().getType().getMaxDurability()-1);
        reader.getHost().setItemMeta(damageable);
        ramming.add(player.getUniqueId());
        player.getWorld().playSound(player.getLocation(),Sound.ITEM_ARMOR_EQUIP_LEATHER,1,1);
        new BukkitRunnable(){
            int count = max;
            @Override
            public void run(){
                if (openInv.contains(uuid)){
                    this.cancel();
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR,new TextComponent("Ramming Stopped"));
                    ramming.remove(player.getUniqueId());
                    return;
                }
                if (dropped.contains(uuid)){
                    dropped.remove(uuid);
                    this.cancel();
                    ramming.remove(player.getUniqueId());
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR,new TextComponent("Ramming Stopped"));
                    return;
                }
                if (slot != inv.getHeldItemSlot()){
                    this.cancel();
                    ramming.remove(player.getUniqueId());
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR,new TextComponent("Ramming Stopped"));
                    return;
                }
                ItemStack offhand = inv.getItemInOffHand();
                if (offhand.getType() != Material.STICK){
                    this.cancel();
                    ramming.remove(player.getUniqueId());
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR,new TextComponent("Ramming Stopped"));
                    return;
                }
                if (count % 20 == 0){
                    player.getWorld().playSound(player.getLocation(),Sound.ITEM_ARMOR_EQUIP_LEATHER,1,1);
                }
                if (count <= 0) {
                    this.cancel();
                    ramming.remove(player.getUniqueId());
                    player.getWorld().playSound(player.getLocation(),Sound.ITEM_ARMOR_EQUIP_LEATHER,1,1);
                    reader.setFiringStage();
                    reader.updateDurability();
                    if (!checkReadyFire(player,reader))
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("Ramming Completed"));
                }
                count--;
                float ratio = (float) count / max;
                Damageable damageable = (Damageable) reader.getItemMeta();
                damageable.setDamage( (int) (reader.getHost().getType().getMaxDurability() * ratio));
                reader.getHost().setItemMeta(damageable);

            }
        }.runTaskTimer(plugin, 1, 1);
    }

    private void fire(Player player, GunReader reader){
        reader.setFiringStage();
        player.getWorld().playSound(player.getLocation(),Sound.ENTITY_GENERIC_EXPLODE,1,1);
        GunBuilder.GunType type = reader.readType();
        double decayRate = Math.max(0, reader.readDecayRate());
        double decayRange = Math.min(0, reader.readDecayRange());
        GunFire gunFire;
        if (type == GunBuilder.GunType.LINE) gunFire = new GunFire(player,reader.readDamage(),reader.readRange(), decayRate,decayRange);
        else gunFire = new SpreadGunFire(player,reader.readDamage(),reader.readRange(),reader.readDecayRate(),reader.readDecayRange(),reader.readProjectiles());
        gunFire.shoot();
        reader.updateDurability(true);
        player.setCooldown(reader.readOGMaterial(),reader.readCooldown());

    }


    private boolean checkReadyFire(Player player, GunReader reader){
        String stage = reader.getFiringStage();
        if (stage.equals("ready to fire")) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("Ready to Fire"));
            return true;
        }else return false;
    }



    @EventHandler
    public void checkOpen(InventoryOpenEvent event){
        openInv.add(event.getPlayer().getUniqueId());
    }
    @EventHandler
    public void checkClose(InventoryCloseEvent event){
        openInv.remove(event.getPlayer().getUniqueId());
    }
    @EventHandler
    public void onQuit(PlayerQuitEvent event){
        openInv.remove(event.getPlayer().getUniqueId());
    }
    @EventHandler
    public void dropGun(PlayerDropItemEvent event){
        dropped.add(event.getPlayer().getUniqueId());
    }


    @EventHandler
    public void swapHand(PlayerSwapHandItemsEvent event){
        Player player = event.getPlayer();
        ItemStack main = event.getMainHandItem();

        passiveCheck(player);
        if (main == null || main.getType().isAir() || !main.hasItemMeta()) return;
        ItemMeta meta = main.getItemMeta();
        if (isGun(meta)) {
           GunReader reader = new GunReader(main, meta);
           setCooldown(player,reader);
        }
    }

    @EventHandler
    public void selectItem(PlayerItemHeldEvent event){
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItem(event.getNewSlot());
        passiveCheck(player);
        if (item == null || item.getType().isAir() || !item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();
        if (isGun(meta)){
            GunReader reader = new GunReader(item, meta);
            setCooldown(player,reader);
        }
    }

    private int current(Player player){
        return player.getCooldown(Material.CROSSBOW);
    }
    @EventHandler
    public void clickHands(InventoryClickEvent event){
        Player player = (Player) event.getWhoClicked();
        // ItemStack mainhand = player.getInventory().getItemInMainHand();

        ItemStack mainhand = player.getInventory().getItemInMainHand();
        ItemStack cursor = event.getCursor();
        passiveCheck(player);
        if (cursor != null && !cursor.getType().isAir() && cursor.hasItemMeta()) {
            ItemMeta meta = cursor.getItemMeta();
            //Case where a twohanded weapon was just placed into the mainhand slot via mouseclick while an item is in the offhand
            if (isGun(meta) && event.getSlot() == player.getInventory().getHeldItemSlot()) {
                GunReader reader = new GunReader(cursor, meta);
                setCooldown(player,reader);
                return;
            }
        }
        if (!mainhand.getType().isAir() && mainhand.hasItemMeta()){
            ItemMeta meta = mainhand.getItemMeta();
            if (event.getSlotType() == InventoryType.SlotType.QUICKBAR && event.getSlot() == 40){
                if (isGun(meta)){
                    GunReader reader = new GunReader(mainhand,meta);
                    setCooldown(player,reader);
                }
            }
        }
        //Case where offhand slot is clicked while a twohanded weapon is in the mainhand


        if (event.getClick() == ClickType.NUMBER_KEY || event.getClick() == ClickType.SHIFT_LEFT){
            ItemStack current = event.getCurrentItem();
            if (current == null || current.getType().isAir() || !current.hasItemMeta()) return;
            ItemMeta meta = current.getItemMeta();
            if (!isGun(meta)) return;
            GunReader reader = new GunReader(current, meta);
            if (event.getHotbarButton() == player.getInventory().getHeldItemSlot()){
                setCooldown(player,reader);
            }
            if (event.getSlot() > 8 && player.getInventory().firstEmpty() == player.getInventory().getHeldItemSlot()){
                setCooldown(player,reader);
            }
        }

    }

    @EventHandler
    public void inv(InventoryDragEvent event){
        Player player = (Player) event.getWhoClicked();
        passiveCheck(player);
        //Case where twohanded weapon is "dragged" into main hand slot while there are items in offhand
        //This one doesn't really make sense, but it does apply sometimes since you can technically drag items of 1 stack
        if (event.getInventorySlots().contains(player.getInventory().getHeldItemSlot())){
            ItemStack old = event.getOldCursor();
            if (old.getType().isAir() || !old.hasItemMeta()) return;
            ItemMeta oldMeta = old.getItemMeta();
            if (isGun(oldMeta)){
                GunReader reader = new GunReader(old, oldMeta);
                setCooldown(player,reader);
            }
        }
    }


    /**
     *
     * @param player
     */
    private void passiveCheck(Player player){
        Bukkit.getScheduler().runTaskLater(plugin,()->{
            ItemStack main = player.getInventory().getItemInMainHand();
            if (main.getType().isAir() || !main.hasItemMeta()) return;
            ItemMeta meta = main.getItemMeta();
            if (!(isGun(meta))){
                GunReader reader = new GunReader(main, meta);
                setCooldown(player,reader);
            }
        } ,1);
    }

    private void setCooldown(Player player, GunReader reader){
        int equipCd = reader.equipCooldown();
        if (equipCd == -1) return;
        if (reader.getFiringStage().equals("ready to fire") && player.hasCooldown(Material.CROSSBOW)) return;
        else player.setCooldown(Material.CROSSBOW,reader.equipCooldown());
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event){
        Player player = (Player) event.getPlayer();
        ItemStack cursor = player.getItemOnCursor();
        if (cursor.getType().isAir() || !cursor.hasItemMeta()) return;
        ItemMeta meta = cursor.getItemMeta();
        if (isGun(meta) && player.getInventory().firstEmpty() == player.getInventory().getHeldItemSlot()){
            GunReader reader = new GunReader(cursor, meta);
            setCooldown(player,reader);
        }
    }


    private boolean isGun(ItemMeta meta){
        return meta.getPersistentDataContainer().has(plugin.gun, PersistentDataType.STRING);
    }


}
