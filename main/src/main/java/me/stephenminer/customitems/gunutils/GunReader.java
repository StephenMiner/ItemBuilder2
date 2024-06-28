package me.stephenminer.customitems.gunutils;

import me.stephenminer.customitems.CustomItems;
import me.stephenminer.customitems.builder.GunBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CrossbowMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class GunReader {

    private final CustomItems plugin;
    private final ItemMeta meta;
    private final ItemStack host;


    /**
     *
     * @param meta ItemMeta that is already confirmed to contain the "gun" type attribute
     */
    public GunReader(ItemStack host, ItemMeta meta){
        this.plugin = JavaPlugin.getPlugin(CustomItems.class);
        this.host = host;
        this.meta = meta;
        loadStages();
    }




    public GunBuilder.GunType readType(){
        String data = meta.getPersistentDataContainer().get(plugin.gun, PersistentDataType.STRING).toUpperCase(Locale.ROOT);
        try{
            return GunBuilder.GunType.valueOf(data);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
    public String readPowder(){
        return meta.getPersistentDataContainer().get(plugin.powder,PersistentDataType.STRING);
    }

    public String readAmmo(){
        return meta.getPersistentDataContainer().get(plugin.ammo, PersistentDataType.STRING);
    }

    public double readDamage(){
        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (container.has(plugin.gunDamage,PersistentDataType.DOUBLE))
            return container.get(plugin.gunDamage,PersistentDataType.DOUBLE);
        return -1;
    }

    public double readRange(){
        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (container.has(plugin.range,PersistentDataType.DOUBLE))
            return container.get(plugin.range, PersistentDataType.DOUBLE);
        return -1;
    }

    public double readDecayRange(){
        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (container.has(plugin.decay,PersistentDataType.DOUBLE))
            return container.get(plugin.decay, PersistentDataType.DOUBLE);
        return -1;
    }

    public double readDecayRate(){
        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (container.has(plugin.decayRate,PersistentDataType.DOUBLE))
            return container.get(plugin.decayRate, PersistentDataType.DOUBLE);
        return -1;
    }

    public int readRamTime(){
        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (container.has(plugin.ramTime,PersistentDataType.INTEGER))
            return container.get(plugin.ramTime,PersistentDataType.INTEGER);
        else return -1;
    }
    public int readProjectiles(){
        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (container.has(plugin.projectiles,PersistentDataType.INTEGER))
            return container.get(plugin.projectiles,PersistentDataType.INTEGER);
        else return -1;
    }
    public int readCooldown(){
        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (container.has(plugin.triggerCooldown, PersistentDataType.INTEGER))
            return container.get(plugin.triggerCooldown,PersistentDataType.INTEGER);
        else return -1;
    }

    public float readIgnoreArmor(){
        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (container.has(plugin.gunIgnoreArmor,PersistentDataType.FLOAT))
            return container.get(plugin.gunIgnoreArmor, PersistentDataType.FLOAT);
        else return 1;
    }
    public Material readOGMaterial(){
        PersistentDataContainer container = meta.getPersistentDataContainer();
        Material mat = host.getType();
        if (container.has(plugin.material,PersistentDataType.STRING)){
            mat = Material.matchMaterial(container.get(plugin.material,PersistentDataType.STRING));
        }
        return mat;
    }

    public float readPlayerBonus(){
        PersistentDataContainer container = meta.getPersistentDataContainer();
        return container.getOrDefault(plugin.playerBonus,PersistentDataType.FLOAT,0f);
    }
    public float readMobBonus(){
        PersistentDataContainer container = meta.getPersistentDataContainer();
        return container.getOrDefault(plugin.mobBonus,PersistentDataType.FLOAT,0f);
    }

    /**
     * To be called when it is confirmed that a gun needs powder ( readPowder() != null) )
     * @param powder the item to check
     * @return true if the powder item's persistent data container has an ID tag matching the readPowder() result, else false
     */
    public boolean validPowder(ItemStack powder){
        if (powder == null || powder.getType().isAir() || !powder.hasItemMeta()) return false;
        ItemMeta meta = powder.getItemMeta();
        if (!meta.getPersistentDataContainer().has(plugin.id, PersistentDataType.STRING)) return false;
        String powderId = readPowder();
        return powderId.equals(meta.getPersistentDataContainer().get(plugin.id, PersistentDataType.STRING));
    }

    /**
     * To be called when it is confirmed that a gun needs ammo ( readAmmo() != null )
     * @param ammo the item to check
     * @return true if ammo item's persistent data container has an ID tag matching the readAmmo() result, else false
     */
    public boolean validAmmo(ItemStack ammo){
        if (ammo == null || ammo.getType().isAir() || !ammo.hasItemMeta()) return false;
        ItemMeta meta = ammo.getItemMeta();
        if (!meta.getPersistentDataContainer().has(plugin.id, PersistentDataType.STRING)) return false;
        String ammoId = readAmmo();
        return ammoId.equals(meta.getPersistentDataContainer().get(plugin.id, PersistentDataType.STRING));

    }

    /**
     * Updates the durability of the gun item so it acts as a progress bar if ramrodding option is enabled
     * @param newMeta define whether a new item meta copy should be created or if the internally stored itemmeta can be used
     */
    public void updateDurability(boolean newMeta){
        if (meta instanceof Damageable){
            if (!getFiringStage().equals("ready to fire")) {
                Damageable damageable;
                if (newMeta) {
                     damageable= (Damageable) host.getItemMeta();
                }else damageable = (Damageable) meta;
                damageable.setDamage(host.getType().getMaxDurability() - 1);
                host.setItemMeta(damageable);

            }else{
                ((Damageable) meta).setDamage(0);
                host.setItemMeta(meta);
            }
        }
    }
    public void updateDurability(){
        updateDurability(false);
    }



    public int equipCooldown(){
        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (container.has(plugin.equipCooldown,PersistentDataType.INTEGER))
            return container.get(plugin.equipCooldown,PersistentDataType.INTEGER);
        else return -1;
    }


    /**
    @return possible stages: "prepare powder" "prepare ammo" "ramming" "ready to fire"
     **/
    public String getFiringStage(){

        if (!meta.hasLore() || meta.getLore().size() == 0) return invalidStageCase();
        List<String> lore = meta.getLore();
        //firing stage should be the last line of lore
        String stage = ChatColor.stripColor(lore.get(0)).toLowerCase();
        if (firingStages.contains(stage)) return stage;
        else return invalidStageCase();
    }


    /**
     * Edits the gun item state to move it to next firing stage (ie from empty to powder loaded)
     * Right now you can't change the order of stages, it is defined by findNextStage()
     */
    public void setFiringStage(){
        List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
        String current = getFiringStage();
        String nextState = findNextStage();
        if (lore.isEmpty())
            lore.add(ChatColor.YELLOW + nextState);
        else if(lore.get(0).equals(ChatColor.YELLOW + current))
            lore.set(0, ChatColor.YELLOW + nextState);
        else lore.add(0,ChatColor.YELLOW + nextState);
        meta.setLore(lore);
        host.setItemMeta(meta);
        changeItemType(nextState);

    }
    public void setFiringStage(String stage){
        List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
        String current = getFiringStage();
        if (lore.isEmpty())
            lore.add(ChatColor.YELLOW + stage);
        else if(lore.get(0).equals(ChatColor.YELLOW + current))
            lore.set(0, ChatColor.YELLOW + stage);
        else lore.add(0,ChatColor.YELLOW + stage);
        meta.setLore(lore);
        host.setItemMeta(meta);
        changeItemType(stage);
    }

    /**
     * Changes the gun item type to a crossbow when the gun is ready to fire for texture pack reasons
     **/
    private void changeItemType(String stage){
        if (stage.equals("ready to fire")){
            Bukkit.getScheduler().runTaskLater(plugin,()->{
                host.setType(Material.CROSSBOW);
                CrossbowMeta meta = (CrossbowMeta) host.getItemMeta();
                meta.addChargedProjectile(new ItemStack(Material.ARROW));
                host.setItemMeta(meta);
            }, 2);

        }else{
            Material mat = readOGMaterial();
            if (mat != host.getType()) host.setType(mat);
        }
    }

    /**
     * Finds the next possible firing state for the gun
     * @return best firing state based on current state
     */
    public String findNextStage(){
        String current = getFiringStage();
        String ammo = readAmmo();
        String powder = readPowder();
        int ramTime = readRamTime();
        switch (current) {
            case "prepare powder"-> {

                if (ammo != null) return "prepare ammo";
                else if (powder != null) return "prepare ramming";
                else if (ramTime > -1) return "ramming";
                else return "ready to fire";
            }
            case "prepare ammo" -> {

                if (ramTime > -1) return "ramming";
                else return "ready to fire";
            }
            case "ramming" -> {
                return "ready to fire";
            }
            case "ready to fire"-> {
                if (powder != null) return "prepare powder";
                else if (ammo != null) return "prepare ammo";
                else if (ramTime > -1) return "ramming";
                else return "return to fire";
            }
        }
        return "ready to firer";
    }

    /**
     * To be used if the gun item doesn't have any firing stage attached to it
     * @return the first available firing stage the gun can have
     */
    private String invalidStageCase(){
        String ammo = readAmmo();
        String powder = readPowder();
        int ramTime = readRamTime();
        if (powder != null) return "prepare powder";
        else if (ammo != null) return "prepare ammo";
        else if (ramTime > -1) return "ramming";
        else return "ready to fire";
    }



    private Set<String> firingStages;


    private void loadStages(){
        firingStages = new HashSet<>();
        firingStages.add("prepare powder");
        firingStages.add("prepare ammo");
        firingStages.add("ramming");
        firingStages.add("ready to fire");
    }

    public ItemStack getHost(){ return host; }
    public ItemMeta getItemMeta(){ return meta; }
    public boolean infinity(){ return meta.hasEnchant(Enchantment.ARROW_INFINITE); }

}
