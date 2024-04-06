You can create items with the /createItem [id] [material] and then /itembuilder command.


Alternatively, you can create a new file in the items folder inside this plugins datafolder titled "[id].yml" 

When editing attributes, it is important to note that you are always adding onto the players base attributes. For example to have a weapon have the same attack speed as a diamond axe, you need to have the generic attack speed value be set to -3 since the player has a base 4 attack speed. If you want no attack cooldown, set the value to like 16.

Here is an examle of an item config file. Keep in mind that "reach", "mount-multiplier", "two-handed", and "custom-model-data" cannot be edited through commands right now so you got to do that here. Also if you dont want these things changed for your items, don't add them in!
```
material: DIAMOND_SWORD
reach: 9
two-handed: true
display-name: '&6Display Name'
custom-model-data: 22314
mount-multiplier: 9
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
