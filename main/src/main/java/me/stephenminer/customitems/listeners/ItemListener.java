package me.stephenminer.customitems.listeners;

import me.stephenminer.customitems.CustomItems;
import me.stephenminer.customitems.builder.ItemReader;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class ItemListener implements Listener {
    private final CustomItems plugin;


    public ItemListener(){
        this.plugin = JavaPlugin.getPlugin(CustomItems.class);
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void cancelPlace(BlockPlaceEvent event){
        Player player = event.getPlayer();
        ItemStack item = event.getItemInHand();
        if (item.getType().isAir() || !item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();
        if (!canPlace(meta)) event.setCancelled(true);
    }

    @EventHandler
    public void stopNormalDamage(PlayerItemDamageEvent event){
        ItemStack item = event.getItem();
        if (item.getType().isAir() || !item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();
        if (maxUses(meta) == -1) return;
        else event.setCancelled(true);
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onBreak(BlockBreakEvent event){
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        Material mat = item.getType();
        if (mat.isAir() || !item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();
        short maxUses = maxUses(meta);
        if (maxUses == -1) return;
        if (!shouldDamage(event,mat, meta, false)) return;
        ItemReader reader = new ItemReader(item, meta);
        reader.updateMinecraftDamage();
        reader.tryBreakItem(player);
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void armorDmg(EntityDamageEvent event){
        if (event.getEntity() instanceof Player player){
            ItemStack[] items = player.getInventory().getArmorContents();
            for (ItemStack item : items){
                if (item == null || !item.hasItemMeta()) continue;
                ItemMeta meta = item.getItemMeta();
                short max = maxUses(meta);
                if (max == -1) continue;
                if (!shouldDamage(event, item.getType(),meta, true)) return;
                ItemReader reader = new ItemReader(item,meta);
                reader.calculateArmorDmg(event.getDamage(),meta);
                reader.tryBreakItem(player);
            }
        }
    }

    @EventHandler
    public void weaponDmg(EntityDamageByEntityEvent event){
        if (event.getDamager() instanceof Player player){
            ItemStack hand = player.getInventory().getItemInMainHand();
            Material mat = hand.getType();
            if (mat.isAir() || !hand.hasItemMeta()) return;
            ItemMeta meta = hand.getItemMeta();
            short maxUses = maxUses(meta);
            if (maxUses == -1) return;
            if (!shouldDamage(event, mat, meta, false)) return;
            ItemReader reader = new ItemReader(hand, meta);
            reader.calculateDmg(meta);
            reader.tryBreakItem(player);
        }
    }

    @EventHandler
    public void bowDamage(EntityShootBowEvent event){
        if (event.getEntity() instanceof Player player){
            ItemStack bow = event.getBow();
            if (bow == null || bow.hasItemMeta()) return;
            ItemMeta meta = bow.getItemMeta();
            short max = maxUses(meta);
            if (max == -1) return;
            ItemReader reader = new ItemReader(bow, meta);
            reader.calculateDmg(meta);
            reader.tryBreakItem(player);
        }
    }






    private boolean canPlace(ItemMeta meta){
        return meta.getPersistentDataContainer().getOrDefault(plugin.placeable, PersistentDataType.BOOLEAN,true);
    }

    private short customDurability(ItemMeta meta){
        return meta.getPersistentDataContainer().getOrDefault(plugin.durability,PersistentDataType.SHORT, (short) -1);
    }

    private short maxUses(ItemMeta meta){
        return meta.getPersistentDataContainer().getOrDefault(plugin.durability,PersistentDataType.SHORT,(short) -1);
    }


    /**
     * @param event
     * @param type the material of the item
     * @param meta ItemMeta from an item that is confirmed to have custom durability
     * @param armor Whether item is in an armor slot or not
     * @return true if the item would normally take damage during the action being taken in an event
     * ie: BlockBreakEvent and the item's material is a tool
     * EntityDamageByEntityEvent and the item has a damage attribute. not really another way to do this
     * EntityDamageEvent if the item is in an armor slot (it is assumed to be so if this method is called for it)
     */
    private boolean shouldDamage(Event event, Material type, ItemMeta meta, boolean armor){
        if (event instanceof EntityDamageEvent damage){
            if (armor) {
                boolean valid = switch (damage.getCause()) {
                    case FIRE, FIRE_TICK, LAVA, MAGIC, FALL, STARVATION, POISON, WITHER, DROWNING -> false;
                    default -> true;
                };
                if (valid) return true;
            }
        }
        if (event instanceof EntityShootBowEvent shoot){
            if (type == Material.BOW || type == Material.CROSSBOW) return true;
        }
        if (event instanceof EntityDamageByEntityEvent damage && damage.getDamager() instanceof Player){
            if (!armor) {
                if (hasAttackAttribute(meta)) return true;
             }
        }
        if (event instanceof BlockBreakEvent breakEvent){
            if (isTool(type)) return true;
        }

        return false;
    }

    /**
     * Checks if the ItemMeta has an attribute associated with melee weapons
     * @param meta
     * @return
     */
    private boolean hasAttackAttribute(ItemMeta meta){
        return !meta.getAttributeModifiers(Attribute.GENERIC_ATTACK_DAMAGE).isEmpty()
                || !meta.getAttributeModifiers(Attribute.GENERIC_ATTACK_SPEED).isEmpty()
                || !meta.getAttributeModifiers(Attribute.GENERIC_ATTACK_KNOCKBACK).isEmpty();
    }


    private boolean isTool(Material mat) {
        return switch (mat){
            case NETHERITE_PICKAXE,DIAMOND_PICKAXE,GOLDEN_PICKAXE,IRON_PICKAXE,STONE_PICKAXE,WOODEN_PICKAXE,
                 NETHERITE_AXE,DIAMOND_AXE,GOLDEN_AXE,IRON_AXE,STONE_AXE,WOODEN_AXE,
                 NETHERITE_SHOVEL,DIAMOND_SHOVEL,GOLDEN_SHOVEL,IRON_SHOVEL,STONE_SHOVEL,WOODEN_SHOVEL,
                 NETHERITE_HOE,DIAMOND_HOE,GOLDEN_HOE,IRON_HOE,STONE_HOE,WOODEN_HOE -> true;
            default -> false;
        };
    }
}
