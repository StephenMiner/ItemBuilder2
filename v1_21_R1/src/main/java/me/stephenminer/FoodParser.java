package me.stephenminer;

import me.stephenminer.customitems.builder.FoodBuilder;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.FoodComponent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Works for versions 1.20.6 (Whenever food components were added -> 1.21.1
 */
public class FoodParser implements FoodBuilder {
    //0 - food,nutrition 1 - effects, 2 - eat seconds, 3 - always can eat, 4 converts-to
    private final String[] source;

    private final ItemMeta meta;
    private final FoodComponent component;
    private final ItemStack item;
    public FoodParser(ItemStack item, ItemMeta base, String[] source){
        this.source = source;
        this.meta = base;
        this.component = meta.getFood();
        this.item = item;
    }


    private void loadStats(){
        int food;
        float saturation;
        if (source[0] == null){
            food = 0;
            saturation = 0;
        }else{
            String[] unpack = source[0].split(",");
            food = Integer.parseInt(unpack[0]);
            saturation = Float.parseFloat(unpack[1]);
        }
        component.setNutrition(food);
        component.setSaturation(saturation);

        float eatSeconds;
        if (source[2] == null || source[2].isBlank()) eatSeconds = 3;
        else eatSeconds = Float.parseFloat(source[2]);
        component.setEatSeconds(eatSeconds);

        boolean alwaysEat;
        if (source[3] == null || source[3].isBlank()) alwaysEat = false;
        else alwaysEat = Boolean.parseBoolean(source[3]);
        component.setCanAlwaysEat(alwaysEat);

    }

    private void loadFoodEffects(){
        if (source[1] == null || source[1].isBlank()) return;
        String[] unpack = source[1].split("/");
        for (String strEffect : unpack){
            buildEffect(strEffect);
        }
    }

    private void setConvertsTo(){
        if (source[4] == null || source[4].isBlank()) return;
        Material mat = Registry.MATERIAL.get(NamespacedKey.minecraft(source[4].toLowerCase()));
        // Not in this api version

    }

    private void buildEffect(String effect){
        String[] unpack = effect.split(",");

        try {
            PotionEffectType type = Registry.EFFECT.get(NamespacedKey.minecraft(unpack[0].toLowerCase()));
            int duration = Integer.parseInt(unpack[1]);
            int amplifier = Integer.parseInt(unpack[2]);
            float chance = Float.parseFloat(unpack[3]);
             component.addEffect(new PotionEffect(type, duration, amplifier), chance);
        }catch (Exception ex){
            System.err.println("FAILED TO PARSE EFFECT: " + effect);
        }
    }

    @Override
    public ItemStack build(){
        loadFoodEffects();
        loadStats();
        meta.setFood(component);
        item.setItemMeta(meta);
        return item;
    }
}
