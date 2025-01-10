package me.stephenminer.customitems.builder;

import me.stephenminer.customitems.CustomItems;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Random;

public class ItemReader {
    private final ItemMeta meta;
    private final ItemStack host;

    private final CustomItems plugin;

    public ItemReader(ItemStack host, ItemMeta meta){
        this.plugin = JavaPlugin.getPlugin(CustomItems.class);
        this.host = host;
        this.meta = meta;
    }




    public short durability(){
        return meta.getPersistentDataContainer().getOrDefault(plugin.durability, PersistentDataType.SHORT, (short) -1);
    }
    public short maxDurability(){
        return meta.getPersistentDataContainer().getOrDefault(plugin.maxUses,PersistentDataType.SHORT,(short)-1);
    }

    public void setDurability(short durability){
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(plugin.durability,PersistentDataType.SHORT, (short) Math.min(durability, maxDurability()));
        updateMinecraftDamage();
    }

    /**
     * Used to display the custom durability on the item's durability bar
     * Sets the host's itemmeta
     */
    public void updateMinecraftDamage(){
        if (meta instanceof Damageable damageable) {
            float ratio = ((float) durability()) / maxDurability();
            short max = host.getType().getMaxDurability();
            int dmg = max - (short) (max * ratio);
            if (dmg >= max) dmg = max - 1;
            damageable.setDamage(dmg);
            host.setItemMeta(meta);
        }
    }



    public void calculateArmorDmg(double dmg, ItemMeta meta){
        short durabilityUsage = (short) Math.max((int) (dmg/4),1);
        int damage = factorUnbreaking(durabilityUsage,true);
        short newVal = (short) Math.max(0,durability() - damage);
        updateDurability(meta, newVal);
        updateMinecraftDamage();
    }

    public void calculateDmg(ItemMeta meta){
        int damage = factorUnbreaking(1,false);
        short newVal = (short)Math.max(0, durability()-damage);

        updateDurability(meta, newVal);
        updateMinecraftDamage();
    }

    public int factorUnbreaking(int damage, boolean armor){
        Random random = new Random();
        int level = meta.getEnchantLevel(Enchantment.DURABILITY);
        int finalDamage = damage;
        for (int i = 0; i < damage; i++){
            if (unbreakingProc(level, armor, random)) finalDamage--;
        }
        return Math.max(0, finalDamage);
    }


    public void tryBreakItem(Player player){
        short durability = durability();
        if (durability > 0) return;
        World world = player.getWorld();
        Location loc = player.getEyeLocation();
        if (host == null || host.getType().isAir()) return;
        world.spawnParticle(Particle.ITEM_CRACK,loc,1, host);
        world.playSound(loc, Sound.ENTITY_ITEM_BREAK,1,1);
        host.setAmount(0);
    }

    /**
     * Unbreaking formula from <a href="https://minecraft.fandom.com/wiki/Durability">here</a>
     * I also tried to verify it in the NMS classes, looked mostly right
     * @param level
     * @param armor
     * @param random
     * @return
     */
    private boolean unbreakingProc(int level, boolean armor, Random random){
        double chance;
        //chance is the chance that durability should be taken
        if (armor) chance = (60 + 40d/(level + 1)) / 100d;
        else chance = (100d/(level + 1)) / 100d;
        double roll = random.nextDouble();
        return roll > chance;
    }

    /**
     *
     * @param meta itemmeta to write value to (make sure to update original item)
     * @param newVal value to write, function will not write value if newVal < 0. If newVal is more than max durability, it will write the max value
     */
    public void updateDurability(ItemMeta meta, short newVal){
        if (newVal < 0) return;
        List<String> lore = meta.getLore();
        if (lore != null) {
            // Really not O(n) bcs durability string should be around the last entry
            for (int i = lore.size() - 1; i >= 0; i--) {
                String entry = ChatColor.stripColor(lore.get(i));
                if (entry.contains("Uses:")) {
                    lore.set(i, ChatColor.GRAY + "Uses: " + newVal);
                    break;
                }
            }
            meta.setLore(lore);
        }
        meta.getPersistentDataContainer().set(plugin.durability,PersistentDataType.SHORT,(short) Math.min(newVal, maxDurability()));
    }



}
