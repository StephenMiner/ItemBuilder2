package me.stephenminer.customitems.builder;

import me.stephenminer.customitems.CustomItems;
import me.stephenminer.customitems.ItemConfig;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.stream.Collectors;

public class BuildAttribute{
    private CustomItems plugin;
    private String id;
    private ItemConfig config;
    public BuildAttribute(CustomItems plugin, ItemConfig config){
        this.plugin = plugin;
        this.config = config;
    }

    private Set<String> getConfigurationSection(String section){
        if (!config.getConfig().contains(section))
            return null;
        return config.getConfig().getConfigurationSection(section).getKeys(false);
    }
    public Set<String> acceptableStrings(){
        Set<String> names = new HashSet<>();
        Attribute[] attributes = Attribute.values();
        for (Attribute attri : attributes) names.add(attri.name());
        return names;
    }

    public Map<Attribute, Double> getAttributes(){
        Set<String> section = getConfigurationSection("attributes");
        if (section == null || section.isEmpty())
            return null;
        Map<Attribute,Double> map = new HashMap<>();
        for (String key : section){
            if (key == null || key.isEmpty())
                continue;
            Attribute a;
            try{
                a = acceptableStrings().contains(key) ? Attribute.valueOf(key) : null;
            }catch (Exception e){
                a = Registry.ATTRIBUTE.get(NamespacedKey.minecraft(key));
            }
            if (a == null) {
                try {
                    a = Registry.ATTRIBUTE.get(NamespacedKey.minecraft(key));
                }catch (Exception ignored){}
            }
            if (a != null) {
                double i = config.getConfig().getDouble("attributes." + key + ".amount");
                map.put(a, i);
            }else System.out.println("FAILED TO READ ATTRIBUTE " + key + "! Maybe it isn't correct?");
        }
        return map;
    }

    public ItemStack addAttributes(ItemStack item){
        ItemStack items = item;
        ItemMeta meta = items.getItemMeta();
        Set<Attribute> set = getAttributes() != null ? getAttributes().keySet() : new HashSet<>();
        if (set.isEmpty())
            return item;
        for (Attribute attribute : set) {
            if (attribute == null)
                continue;
            meta.addAttributeModifier(attribute, translateAttribute(attribute));
        }
        items.setItemMeta(meta);
        return items;


    }


    public EquipmentSlot getSlot(Attribute attribute){
        String sSlot = config.getConfig().getString("attributes." + attribute.name() + ".slot");
        boolean exists = sSlot !=null;
        return exists ? EquipmentSlot.valueOf(sSlot) : EquipmentSlot.HAND;
    }


    public AttributeModifier translateAttribute(Attribute attribute){
        Map<Attribute, Double> map = getAttributes();
        AttributeModifier modifier;
        switch (attribute){
            case GENERIC_ATTACK_DAMAGE:
                modifier = new AttributeModifier(UUID.randomUUID(), "generic.attackDamage", map.get(attribute), AttributeModifier.Operation.ADD_NUMBER, getSlot(attribute));
                return modifier;
            case GENERIC_ARMOR:
                modifier = new AttributeModifier(UUID.randomUUID(), "generic.armor", map.get(attribute), AttributeModifier.Operation.ADD_NUMBER, getSlot(attribute));
                return modifier;
            case GENERIC_ARMOR_TOUGHNESS:
                modifier = new AttributeModifier(UUID.randomUUID(), "generic.armorToughness", map.get(attribute), AttributeModifier.Operation.ADD_NUMBER, getSlot(attribute));
                return modifier;
            case GENERIC_ATTACK_KNOCKBACK:
                modifier = new AttributeModifier(UUID.randomUUID(), "generic.attackKnockback", map.get(attribute), AttributeModifier.Operation.ADD_NUMBER, getSlot(attribute));
                return modifier;
            case GENERIC_ATTACK_SPEED:
                modifier = new AttributeModifier(UUID.randomUUID(), "generic.attackSpeed", map.get(attribute), AttributeModifier.Operation.ADD_NUMBER, getSlot(attribute));
                return modifier;
            case GENERIC_MOVEMENT_SPEED:
                modifier = new AttributeModifier(UUID.randomUUID(), "generic.movementSpeed", map.get(attribute), AttributeModifier.Operation.ADD_NUMBER, getSlot(attribute));
                return modifier;
            case GENERIC_KNOCKBACK_RESISTANCE:
                modifier = new AttributeModifier(UUID.randomUUID(), "generic.knockbackResistance", map.get(attribute), AttributeModifier.Operation.ADD_NUMBER, getSlot(attribute));
                return modifier;
            case GENERIC_MAX_HEALTH:
                modifier = new AttributeModifier(UUID.randomUUID(), "generic.maxHealth", map.get(attribute), AttributeModifier.Operation.ADD_NUMBER, getSlot(attribute));
                return modifier;
            case GENERIC_LUCK:
                modifier = new AttributeModifier(UUID.randomUUID(), "generic.luck", map.get(attribute), AttributeModifier.Operation.ADD_NUMBER, getSlot(attribute));
                return modifier;
            case GENERIC_FLYING_SPEED:
                modifier = new AttributeModifier(UUID.randomUUID(), "generic.flyingSpeed", map.get(attribute), AttributeModifier.Operation.ADD_NUMBER, getSlot(attribute));
                return modifier;
        }
        Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Attempted to create attribute modifier for attribute " + attribute.name() + ", " + attribute.name() + " is not supported!");
        return null;
    }


}