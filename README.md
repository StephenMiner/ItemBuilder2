Supported Versions: 1.20.2,1.20.4


***NOTE***
The recipe function is currently untested in the current state and probably doesn't work. When will I fix this, idk whenever someone says they need it probably. Also some commands also dont really matter anymore such as /itembuilder reload - You should never need to use this command. You can create and edit item files at your leisure.










You can create items with the /createItem [id] [material] and then /itembuilder command.


Alternatively, you can create a new file in the items folder inside this plugins datafolder titled "[id].yml" 

When editing attributes, it is important to note that you are always adding onto the players base attributes. For example to have a weapon have the same attack speed as a diamond axe, you need to have the generic attack speed value be set to -3 since the player has a base 4 attack speed. If you want no attack cooldown, set the value to like 16.

In terms of the proper naming for things like Attributes and Enchants, you can check the tab completer for the itembuilder command since it will show all of them. Same goes for material names.

Here is an examle of an item config file. Keep in mind that "reach", "mount-multiplier", "two-handed", and "custom-model-data" cannot be edited through commands right now so you got to do that here. Also if you dont want these things changed for your items, don't add them in!
```
material: DIAMOND_SWORD
reach: 9
two-handed: true
display-name: '&6Display Name'
custom-model-data: 22314
##Damage multiplier applied when the attacker is riding a mount
mount-multiplier: 9
shield-breaker-ticks: 40
##Defines a damage bonus when players are struck by this weapon. formula is damage + (damage*multiplier)
##If weapon is a gun, unless item has a 'ranged-melee: true' on file, then bonus is only applied to the gun's ranged attacks
player-bonus: 0.25
##Defines a damage bonus when mobs are struck by this weapon. formula is damage + (damage*multiplier)
##If weapon is a gun, unless item has a 'ranged-melee: true' on file, then bonus is only applied to the gun's ranged attacks
mob-bonus: 9
##Defines how much armor damage absorption should be reduced by as a %, a value from 0.0-1.0 with 0.0 representing full armor piercing.
ap: 0.5
attributes:
  GENERIC_MAX_HEALTH:
    amount: 234.0
    slot: HAND
  GENERIC_ATTACK_DAMAGE:
    amount: 10.0
    slot: HAND
  GENERIC_ATTACK_SPEED:
    amount: -3
    slot: HAND
  GENERIC_ARMOR:
    amount: 23.0
    slot: CHEST
  GENERIC_ARMOR_TOUGHNESS:
    amount: 23.0
    slot: CHEST
  GENERIC_ATTACK_KNOCKBACK:
    amount: 23.0
    slot: CHEST
  GENERIC_KNOCKBACK_RESISTANCE:
    amount: 23.0
    slot: CHEST
enchantments:
  silk_touch:
    level: 4
lore:
- line_1
- line_2
- line_3
```

All loading functions related to guns are completed via the offhand key (sorry no options for that)
All firing is to be done with a right click
When a gun is loaded, for resource pack reasons, the item will be changed to a crossbow

here is an example of a gun config:
```
material: DIAMOND_HOE
##Can be either spread or line, spread will create a shotgun like effect while line is a single straight line
gun-type: spread
##The item id that can be used as ammo. IDs should be the names of other item files. 
##If not present, the "load ammo" firing stage will be skipped
ammo: musketball
##The item id that can be used as powder. IDs should be the names of other item files
##If not present, the "load powder" firing stage will be skipped
powder: blackpowder
##How much raw damage the each bullet should do
gun-damage: 4
##Range of the gun
range: 50
##[Not Required] The amount of blocks the bullet will travel before damage decay is applied
decay-range: 25
##[Not Required] The rate per block at which damage will decay after the decay-range is hit
decay-rate: 0.05
##How much time in ticks it should take to ram the powder and shot into the gun
##If not present, the "ramming" firing stage will be skipped
ram-time: 60
##[Not Needed] Cooldown placed on the weapon when it is shot/triggered
trigger-cooldown: 100
##[Not needed] The cooldown applied (in ticks) when the weapon is equiped
##equip-cooldown: 2
##Only applicable for gun-type: spread
##Defines how much projectiles will be shot in a singular use (ammo costs still remain at 1)
projectiles: 6
##Defines how much armor damage absorption should be reduced by as a %, a value from 0.0-1.0 with 0.0 representing full armor piercing.
gunap: 1
##This number defines the spread for SPREAD-type guns. 
##Think of this number of the radius of the cone-of-fire that your gun will shoot in terms of blocks
gun-spread: 0.5
```
You do not have to define decay-ranges or decay-rates.
You don't need to define ammo or powder or ram-time, but without them the gun will be able to fire without anything or without whatever you didn't include.
projectile should only be used if you have ```gun-type: spread``` the other option is ```gun-type: line```

Here is an example of a ```gun-type: SPREAD``` weapon:  


https://github.com/StephenMiner/ItemBuilder2/assets/64863697/faceab3a-a83a-410b-99d5-085f650c3d2a

