package me.stephenminer.v1_21_R3;

import me.stephenminer.customitems.builder.FoodBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_21_R2.CraftSound;
import org.bukkit.craftbukkit.v1_21_R2.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_21_R2.potion.CraftPotionUtil;

import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Version specific as in newer versions, stuff is separated into the Consumable Component
 * This component does not have a spigot api counterpart.
 */
public class FoodParser implements FoodBuilder {

    //0 - food,nutrition 1 - effects, 2 - eat seconds, 3 - always can eat, 4 converts-to
    private final String[] source;

    private final ItemStack item;
    public FoodParser(org.bukkit.inventory.ItemStack item, String[] source){
        this.source = source;

        this.item = CraftItemStack.asNMSCopy(item);
    }


    private FoodProperties loadStats(){
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

        //The below isn't present in the api
        /*
        float eatSeconds;
        if (source[2] == null || source[2].isBlank()) eatSeconds = 3;
        else eatSeconds = Float.parseFloat(source[2]);
        component.setEatSeconds(eatSeconds);

         */

        boolean alwaysEat;
        if (source[3] == null || source[3].isBlank()) alwaysEat = false;
        else alwaysEat = Boolean.parseBoolean(source[3]);
        return new FoodProperties(food, saturation, alwaysEat);
    }

    private Consumable loadFoodEffects(){
        if (source[1] == null || source[1].isBlank()) return null;
        String[] unpack = source[1].split("/");
        int i = 0;
        Consumable.Builder builder = Consumable.builder();
        builder.animation(ItemUseAnimation.EAT);
        builder.hasConsumeParticles(true);
        builder.sound(Holder.direct(CraftSound.bukkitToMinecraft(Sound.ENTITY_GENERIC_EAT)));
        builder.soundAfterConsume(Holder.direct(CraftSound.bukkitToMinecraft(Sound.ENTITY_PLAYER_BURP)));

        for (String strEffect : unpack){
            buildEffect(builder, strEffect);
        }
       // item.applyComponents(DataComponentPatch.builder().set(DataComponentType.builder().persistent(Codec).build(), builder.build()).build());
        return builder.build();
    }

    private void setConvertsTo(){
        if (source[4] == null || source[4].isBlank()) return;
        Material mat = Registry.MATERIAL.get(NamespacedKey.minecraft(source[4].toLowerCase()));
        // Not in this api version
    }

    private void buildEffect(Consumable.Builder builder, String effect){
        String[] unpack = effect.split(",");
        try {
            PotionEffectType type = Registry.EFFECT.get(NamespacedKey.minecraft(unpack[0].toLowerCase()));
            int duration = Integer.parseInt(unpack[1]);
            int amplifier = Integer.parseInt(unpack[2]);
            float chance = Float.parseFloat(unpack[3]);
            PotionEffect potEffect = new PotionEffect(type, duration,amplifier);
            builder.onConsume(new ApplyStatusEffectsConsumeEffect(CraftPotionUtil.fromBukkit(potEffect), chance));
            //component.addEffect(new PotionEffect(type, duration, amplifier), chance);
        }catch (Exception ex){
            System.err.println("FAILED TO PARSE EFFECT: " + effect);
        }
    }

    @Override
    public org.bukkit.inventory.ItemStack build(){
        Consumable consumable = loadFoodEffects();
        FoodProperties food = loadStats();
        item.applyComponents(DataComponentPatch.builder().set(DataComponents.CONSUMABLE,consumable).build());
        item.applyComponents(DataComponentPatch.builder().set(DataComponents.FOOD, food).build());
        return CraftItemStack.asBukkitCopy(item);
    }
}
