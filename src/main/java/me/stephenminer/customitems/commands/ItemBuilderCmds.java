package me.stephenminer.customitems.commands;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import me.stephenminer.customitems.CustomItems;
import me.stephenminer.customitems.ItemConfig;
import me.stephenminer.customitems.builder.ItemBuilder;
import me.stephenminer.customitems.inventories.RecipeDisplay;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class ItemBuilderCmds implements CommandExecutor {
    private CustomItems plugin;
    private ItemConfig config;

    public ItemBuilderCmds(CustomItems plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("itembuilder")) {
            if (args.length < 1)
                return false;
            String arg = args[0];
            int i = args.length;
            if (arg.equalsIgnoreCase("reloadconfig")) {
                if (!sender.hasPermission("itembuilder.cmds.reload"))
                    return false;
                plugin.Items.reloadConfig();
                plugin.Recipes.reloadConfig();
                plugin.Items.saveConfig();
                plugin.Recipes.saveConfig();
                sender.sendMessage(ChatColor.GREEN + "Reloaded Files");
                return true;
            }

            if (arg.equalsIgnoreCase("recipe")) {
                if (!sender.hasPermission("itembuilder.cmds.recipe"))
                    return false;
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "Only players can use this command");
                    return false;
                }
                if (i < 2) {
                    sender.sendMessage(ChatColor.RED + "You need to say which item you want to create/edit the recipe for!");
                    return false;
                }
                String id = args[1];
                Player player = (Player) sender;
                RecipeDisplay recipeDisplay = new RecipeDisplay(plugin, id);
                player.openInventory(recipeDisplay.display());
                sender.sendMessage(ChatColor.GREEN + "Reloaded Files");
                return true;
            }

            if (arg.equalsIgnoreCase("give")) {
                if (!sender.hasPermission("itembuilder.cmds.give"))
                    return false;
                if (i < 3) {
                    if (!(sender instanceof Player)) {
                        sender.sendMessage(ChatColor.RED + "Sorry, but only players can recieve items!");
                        return false;
                    }
                    if (i == 1) {
                        sender.sendMessage(ChatColor.RED + "You need to say what item you want to give! If no items show up in the tab completer you have no items to give!");
                        return false;
                    }
                    Player player = (Player) sender;
                    giveItem(args[1], player);
                    return true;
                }
                String item = args[2];
                String pname = args[1];
                giveItemPlayer(item, sender, pname);
                return true;
            }
            config = plugin.findConfig(args[1]);
            if (config == null){
                sender.sendMessage(ChatColor.RED + args[1] + " is not a valid item");
                return false;
            }
            if (arg.equalsIgnoreCase("addEnchantment")) {
                int level = 1;
                if (i > 3)
                    level = Integer.parseInt(args[3]);
                Enchantment ench = Enchantment.getByKey(NamespacedKey.minecraft(args[2]));
                if (ench == null) {
                    sender.sendMessage(ChatColor.RED + "inputted enchantment is not valid");
                    return false;
                }
                sender.sendMessage(ChatColor.GREEN + "Added enchantment!");
                return addEnchantment(ench, level);
            }


            if (arg.equalsIgnoreCase("addAttribute")) {
                double value = 0;
                if (i < 5) {
                    sender.sendMessage(ChatColor.RED + "Value modifier not present, please input value modifier after your attribute");
                    return false;
                }
                String equipmentSlot = args[4];
                if (!attributeAcceptable(args[2])) {
                    sender.sendMessage(ChatColor.RED + "inputted attribute is not valid");
                    return false;
                }
                if (!isSlot(args[4])) {
                    sender.sendMessage(ChatColor.RED + "inputted equipmentslot isn't valid");
                    return false;
                }

                Attribute attrib = Attribute.valueOf(args[2]);
                value = Double.parseDouble(args[3]);
                sender.sendMessage(ChatColor.GREEN + "Added attribute!");
                return addAttribute(attrib, value, equipmentSlot);
            }


            if (arg.equalsIgnoreCase("removeAttribute")) {
                if (i < 3) {
                    sender.sendMessage(ChatColor.RED + "You need to say which attribute you would like to remove");
                    return false;
                }
                if (!attributeAcceptable(args[2])) {
                    sender.sendMessage(ChatColor.RED + "inputted attribute is not real or valid");
                    return false;
                }
                sender.sendMessage(ChatColor.GREEN + "Removed attribute!");
                return removeAttribute(Attribute.valueOf(args[2]));
            }


            if (arg.equalsIgnoreCase("removeEnchantment")) {
                if (i < 3) {
                    sender.sendMessage(ChatColor.RED + "You need to say which enchantment you would like to remove");
                    return false;
                }
                Enchantment enchantment = Enchantment.getByKey(NamespacedKey.minecraft(args[2]));
                if (enchantment == null) {
                    sender.sendMessage(ChatColor.RED + "inputted enchantment is not real or valid");
                    return false;
                }
                sender.sendMessage(ChatColor.GREEN + "Removed enchantment!");
                return removeEnchantment(enchantment);
            }


            if (arg.equalsIgnoreCase("setUnbreakable")) {
                if (i < 3) {
                    sender.sendMessage(ChatColor.RED + "You need define whether your item will be unbreakable with true/false");
                    return false;
                }

                boolean b = Boolean.parseBoolean(args[2]);
                sender.sendMessage(ChatColor.GREEN + "Added unbreakable value!");
                return setUnbreakable(b);
            }


            if (arg.equalsIgnoreCase("addItemFlag")) {
                if (i < 3) {
                    sender.sendMessage(ChatColor.RED + "You need define the flag you want to add");
                    return false;
                }

                ItemFlag flag = ItemFlag.valueOf(args[2]);
                if (!validFlags().contains(flag.name())) {
                    sender.sendMessage(ChatColor.RED + "Inputted flag isn't real/valid");
                    return false;
                }
                sender.sendMessage(ChatColor.GREEN + "Added unbreakable value!");
                return addFlag(flag);
            }
            if (arg.equalsIgnoreCase("removeItemFlag")) {
                if (i < 3) {
                    sender.sendMessage(ChatColor.RED + "You need define the flag you want to add");
                    return false;
                }

                ItemFlag flag = ItemFlag.valueOf(args[2]);
                if (!validFlags().contains(flag.name())) {
                    sender.sendMessage(ChatColor.RED + "Inputted flag isn't real/valid");
                    return false;
                }
                sender.sendMessage(ChatColor.GREEN + "Removed item-flag!");
                return removeFlag(flag);
            }
            if (arg.equalsIgnoreCase("setLore")) {
                if (i < 3) {
                    sender.sendMessage(ChatColor.RED + "You need to say what you want the lore to be!");
                    return false;
                }
                sender.sendMessage(ChatColor.GREEN + "Added Lore");
                return setLore(args[2]);
            }
            if (arg.equalsIgnoreCase("setDisplayName")) {
                if (i < 3) {
                    sender.sendMessage(ChatColor.RED + "You need to say what you want the display name to be");
                    return false;
                }
                sender.sendMessage(ChatColor.GREEN + "Set Displayname");
                String displayname = args[2].replace('_', ' ');
                return setDisplayName(displayname);
            }
            if (arg.equalsIgnoreCase("removeLore")) {
                if (i < 2) {
                    sender.sendMessage(ChatColor.RED + "You need to say what item you want to remove the lore of");
                    return false;
                }
                sender.sendMessage(ChatColor.GREEN + "removed lore");
                return removeLore(args[1]);
            }
        }
        return false;
    }


    private boolean giveItem(String string, Player sender) {
        ItemBuilder ib = new ItemBuilder(plugin, string);
        if (!ib.hasEntry()) {
            sender.sendMessage(ChatColor.RED + "Item " + string + " isn't contained in files!");
            return false;
        }
        ItemStack item = ib.buildItem();
        if (item == null)
            return false;
        sender.getInventory().addItem(item);
        sender.sendMessage(ChatColor.GREEN + "You have received your item!");
        return true;
    }


    private boolean giveItemPlayer(String item, CommandSender sender, String reciever) {
        Player p = Bukkit.getPlayerExact(reciever);
        if (p == null) {
            sender.sendMessage(ChatColor.RED + "player isn't real!");
            return false;
        }
        if (!p.isOnline()) {
            sender.sendMessage(ChatColor.RED + "player isn't online!");
            return false;
        }
        ItemBuilder ib = new ItemBuilder(plugin, item);
        if (!ib.hasEntry()) {
            sender.sendMessage(ChatColor.RED + "item isn't in files!");
            return false;
        }
        ItemStack ritem = ib.buildItem();
        if (item == null)
            return false;
        p.getInventory().addItem(ritem);
        p.sendMessage(ChatColor.GREEN + "Recieved item!");
        sender.sendMessage(ChatColor.GREEN + "Gave item to " + reciever);
        return true;
    }


    private boolean addEnchantment(Enchantment ench, int level) {
        config.getConfig().set("enchantments." + ench.getKey().getKey() + ".level", level);
        config.saveConfig();
        return true;
    }

    private boolean addAttribute(Attribute attrib, double amount, String slot) {
        config.getConfig().set("attributes." + attrib.name() + ".amount", amount);
        config.getConfig().set("attributes." + attrib.name() + ".slot", slot);
        config.saveConfig();
        return true;
    }

    private boolean isItem(String id) {
        return plugin.findConfig(id) != null;
    }

    private boolean attributeAcceptable(String tester) {
       return attributes().contains(tester);
    }

    private boolean removeAttribute(Attribute attribute) {
        config.getConfig().set("attributes." + attribute.name(), null);
        config.saveConfig();
        return true;
    }

    private boolean removeEnchantment(Enchantment enchantment) {
        config.getConfig().set("enchantments." + enchantment.getKey().getKey(), null);
        config.saveConfig();
        return true;
    }

    private boolean setUnbreakable(boolean unbreakable) {
        config.getConfig().set("unbreakable", unbreakable);
        config.saveConfig();
        return true;
    }

    private boolean addFlag(ItemFlag flag) {
        List<String> currentFlags = config.getConfig().getStringList("item-flags");
        if (currentFlags.contains(flag.name()))
            return true;
        currentFlags.add(flag.name());
        config.getConfig().set("item-flags", null);
        config.saveConfig();
        config.getConfig().set("item-flags", currentFlags);
        config.saveConfig();
        ;
        return true;
    }

    private boolean removeFlag(ItemFlag flag) {
        List<String> currentFlags = config.getConfig().getStringList("item-flags");
        currentFlags.remove(flag.name());
        config.getConfig().set("item-flags", null);
        config.saveConfig();
        config.getConfig().set("item-flags", currentFlags);
        config.saveConfig();
        ;
        return true;
    }

    private Set<String> validFlags() {
        Set<String> flags = new HashSet<>();
        flags.add(ItemFlag.HIDE_DYE.name());
        flags.add(ItemFlag.HIDE_ATTRIBUTES.name());
        flags.add(ItemFlag.HIDE_DESTROYS.name());
        flags.add(ItemFlag.HIDE_ENCHANTS.name());
        flags.add(ItemFlag.HIDE_PLACED_ON.name());
        flags.add(ItemFlag.HIDE_POTION_EFFECTS.name());
        flags.add(ItemFlag.HIDE_UNBREAKABLE.name());
        return flags;
    }

    private boolean setDisplayName(String name) {
        config.getConfig().set("display-name", ChatColor.translateAlternateColorCodes('&', name));
        config.saveConfig();
        return true;
    }

    private boolean setLore(String set) {
        List<String> tempList = Lists.newArrayList(Splitter.on(',').split(set));
        List<String> lore = new ArrayList<>();
        for (String iteration : tempList) {
            lore.add(ChatColor.translateAlternateColorCodes('&', iteration));
        }
        config.getConfig().set("lore", lore);
        config.saveConfig();
        return true;
    }

    private boolean removeLore(String id) {
        config.getConfig().set("lore", null);
        config.saveConfig();
        return true;
    }

    private boolean isSlot(String s) {
        EquipmentSlot[] set = EquipmentSlot.values();
        for (EquipmentSlot slot : set) {
            if (s.equalsIgnoreCase(slot.name()))
                return true;
        }
        return false;
    }

    public Set<String> attributes(){
        return Arrays.stream(Attribute.values()).map(Attribute::name).collect(Collectors.toSet());
    }

}
