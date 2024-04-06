package me.stephenminer.customitems.commands;

import me.stephenminer.customitems.CustomItems;
import me.stephenminer.customitems.ItemConfig;
import me.stephenminer.customitems.builder.BuildAttribute;
import me.stephenminer.customitems.builder.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;

import java.io.File;
import java.util.*;

public class AutoComplete implements TabCompleter {
    private CustomItems plugin;

    public AutoComplete(CustomItems plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        int length = args.length;
        if (!cmd.getName().equalsIgnoreCase("itembuilder"))
            return null;
        if (length == 1)
            return baseCompleter();
        String args1 = args[0];
        if (args1.equalsIgnoreCase("give")) {
            if (length == 3)
                return itemCompleter(args[1]);
            return playerItemCompleter(args[1]);
        }
        if (args1.equalsIgnoreCase("recipe"))
            return itemCompleter(args[1]);
        if (args1.equalsIgnoreCase("addEnchantment")) {
            if (length == 3)
                return addEnchantment(args[1]);
            return itemCompleter(args[1]);
        }
        if (args1.equalsIgnoreCase("addAttribute")) {
            if (length == 3)
                return addAttribute();
            if (length == 4)
                return needInt();
            if (length == 5)
                return slotList();

            return itemCompleter(args[1]);
        }
        if (args1.equalsIgnoreCase("removeEnchantment")) {
            if (length == 3)
                return removeEnchant(args[1], args[2]);
            return itemCompleter(args[1]);
        }
        if (args1.equalsIgnoreCase("removeAttribute")) {
            if (length == 3)
                return removeAttribute(args[1], args[2]);
            return itemCompleter(args[1]);
        }
        if (args1.equalsIgnoreCase("setUnbreakable")) {
            if (length == 3)
                return unbreakable();
            return itemCompleter(args[1]);
        }
        if (args1.equalsIgnoreCase("addItemFlag")) {
            if (length == 3)
                return itemFlags(args[2]);
            return itemCompleter(args[1]);
        }
        if (args1.equalsIgnoreCase("removeItemFlag")) {
            if (length == 3)
                return removeItemFlags(args[1], args[2]);
            return itemCompleter(args[1]);
        }
        if (args1.equalsIgnoreCase("setLore")) {
            if (length == 3)
                return setLore();
            return itemCompleter(args[1]);
        }
        if (args1.equalsIgnoreCase("setDisplayname"))
            return itemCompleter(args[1]);
        if (args1.equalsIgnoreCase("removeDisplayname"))
            return itemCompleter(args[1]);


        return null;
    }

    private List<String> baseCompleter() {
        List<String> completer = new ArrayList<>();
        completer.add("reloadconfig");
        completer.add("give");
        completer.add("recipe");
        completer.add("addAttribute");
        completer.add("addEnchantment");
        completer.add("removeEnchantment");
        completer.add("removeAttribute");
        completer.add("setUnbreakable");
        completer.add("addItemFlag");
        completer.add("removeItemFlag");
        completer.add("setLore");
        completer.add("setDisplayName");
        completer.add("removeLore");
        completer.add("createItem");
        completer.add("removeDisplayname");
        return completer;
    }

    private List<String> itemCompleter(String match) {
        File parent = new File(plugin.getDataFolder().getPath(), "items");
        if (!parent.exists()) parent.mkdir();
        List<String> items = new ArrayList<>();
        String[] fileNames = parent.list();
        for (String name : fileNames){
            items.add(name.replace(".yml",""));
        }
        return plugin.filter(items, match);
    }

    private List<String> playerItemCompleter(String match) {
        List<String> returnList = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            returnList.add(player.getName());
        }
        returnList.addAll(itemCompleter(match));
        return plugin.filter(returnList, match);
    }

    private List<String> unbreakable() {
        List<String> list = new ArrayList<>();
        list.add("true");
        list.add("false");
        return list;
    }

    private List<String> addEnchantment(String match) {
        List<String> enchants = Arrays.stream(Enchantment.values()).map(enchantment -> enchantment.getKey().getKey()).toList();
        return plugin.filter(enchants, match);
    }

    private List<String> addAttribute() {
        List<String> list = new ArrayList<>();
        list.add(Attribute.GENERIC_ATTACK_DAMAGE.name());
        list.add(Attribute.GENERIC_ATTACK_SPEED.name());
        list.add(Attribute.GENERIC_ATTACK_KNOCKBACK.name());
        list.add(Attribute.GENERIC_ARMOR.name());
        list.add(Attribute.GENERIC_KNOCKBACK_RESISTANCE.name());
        list.add(Attribute.GENERIC_LUCK.name());
        list.add(Attribute.GENERIC_MAX_HEALTH.name());
        list.add(Attribute.GENERIC_MOVEMENT_SPEED.name());
        list.add(Attribute.GENERIC_FLYING_SPEED.name());
        list.add(Attribute.GENERIC_ARMOR_TOUGHNESS.name());
        return list;
    }

    private List<String> slotList() {
        EquipmentSlot[] slots = EquipmentSlot.values();
        List<String> set = new ArrayList<>();
        for (EquipmentSlot slot : slots) {
            set.add(slot.name());
        }
        return set;
    }

    private List<String> removeEnchant(String id, String match) {
        ItemBuilder itemBuilder = new ItemBuilder(plugin, id);
        Map<Enchantment, Integer> enchants = itemBuilder.getEnchantments();
        List<String> returnList = new ArrayList<>();
        if (enchants == null || enchants.isEmpty())
            return null;
        for (Enchantment ench : enchants.keySet()) {
            returnList.add(ench.getKey().getKey());
        }
        return plugin.filter(returnList, match);
    }

    private List<String> removeAttribute(String id, String match) {
        ItemConfig config = plugin.findConfig(id);
        if (config == null) return null;
        BuildAttribute buildAttribute = new BuildAttribute(plugin, config);
        Map<Attribute, Double> attributes = buildAttribute.getAttributes();
        List<String> returnList = new ArrayList<>();
        if (attributes == null || attributes.isEmpty())
            return null;
        for (Attribute attrib : attributes.keySet()) {
            returnList.add(attrib.name());
        }
        return plugin.filter(returnList, match);
    }

    private List<String> itemFlags(String match) {
        List<String> flags = new ArrayList<>();
        flags.add(ItemFlag.HIDE_DYE.name());
        flags.add(ItemFlag.HIDE_ATTRIBUTES.name());
        flags.add(ItemFlag.HIDE_DESTROYS.name());
        flags.add(ItemFlag.HIDE_ENCHANTS.name());
        flags.add(ItemFlag.HIDE_PLACED_ON.name());
        flags.add(ItemFlag.HIDE_POTION_EFFECTS.name());
        flags.add(ItemFlag.HIDE_UNBREAKABLE.name());
        return plugin.filter(flags, match);
    }

    private List<String> removeItemFlags(String id, String match) {
        List<String> flags = new ArrayList<>();
        ItemBuilder builder = new ItemBuilder(plugin, id);
        for (ItemFlag itemFlag : builder.getFlags()) {
            flags.add(itemFlag.name());
        }
        return plugin.filter(flags, match);
    }

    private List<String> setLore() {
        List<String> lore = new ArrayList<>();
        lore.add("format: [line,line2,line3]");
        return lore;
    }

    private List<String> needInt() {
        List<String> list = new ArrayList<>();
        list.add("[number]");
        return list;
    }
}