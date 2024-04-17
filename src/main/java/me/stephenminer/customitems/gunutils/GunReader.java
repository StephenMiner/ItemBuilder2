package me.stephenminer.customitems.gunutils;

import me.stephenminer.customitems.CustomItems;
import me.stephenminer.customitems.builder.GunBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
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
    public Material readOGMaterial(){
        PersistentDataContainer container = meta.getPersistentDataContainer();
        Material mat = host.getType();
        if (container.has(plugin.material,PersistentDataType.STRING)){
            mat = Material.matchMaterial(container.get(plugin.material,PersistentDataType.STRING));
        }
        return mat;
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

    public void updateDurability(){
        if (meta instanceof Damageable){
            if (!getFiringStage().equals("ready to fire")) {
                ((Damageable) meta).setDamage(host.getType().getMaxDurability() - 1);
                host.setItemMeta(meta);
            }else{
                ((Damageable) meta).setDamage(0);
                host.setItemMeta(meta);
            }
        }
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

    public void setFiringStage(){
        List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
        String nextState = findNextStage();
        if (lore.isEmpty())
            lore.add(ChatColor.YELLOW + "" + ChatColor.BOLD + nextState);
        else lore.set(0, ChatColor.YELLOW + "" + ChatColor.BOLD + nextState);
        meta.setLore(lore);
        host.setItemMeta(meta);
        changeItemType(nextState);

    }
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

}
