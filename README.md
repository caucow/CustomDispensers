# CustomDispensers

Lets you add custom dropper and dispenser "recipes".

Note: BlockDispenseEvent which this plugin relies on is... *extremely broken*.<BR>
Be cautious and read the docs when using variable items in recipe actions.<BR>
Also know that if a recipe cancels the event (as most should), you will need manually program the dispenser to consume
the dispensed item using the `consume_item` action.

### Planned Features
- `attack_entity` - This action is not fully supported yet and will be added soon.
- Better item matching - Currently a recipe item only matches if everything except the item count matches the given
  item. I want add the ability to use an NBT matcher, so rather than being forced to use an exact item, only some tags
  in the item will need to match. This will be useful in `break_block` and `attack_entity` actions so damaged pickaxes
  or swords can be used.

## Commands

- `/customdispensers reload` - Admin command. Reloads the plugin configuration.
- `/itemyaml` - Prints out (both in-game and in console) the serialized YAML form of the held item (convenience command
  for setting up the config).

## Permissions

| Permission | Description |
| --- | --- |
| customdispensers.command.customdispensers | Gives access to /customdispensers |
| customdispensers.command.itemyaml | Gives access to /itemyaml |
| customdispensers.recipe.\<permission\> | Lets a player use a permission-gated recipe |

## Configuration

The configuration has 2 main sections, `recipes` and `items`, defining the dispenser recipes and named items used in
them. Recipes consist of filters that decide which recipe to use and actions to take once a recipe matches. Some actions
have extra parameters that can either be directly defined in the config or use variables based on the context the recipe
is running in (ex. an `add_item` action can use the dispensed item by using `$item.dispensed` as its argument).

Valid variable arguments for actions include:

- `$item.dispensed` - the item dropped/dispensed/used by the dispenser. If the BlockDispenseEvent is canceled, modifying
  this item will have no effect, use `$item.dispenser` instead.
- `$item.dispenser` - an item *in the dispenser's inventory* similar to the dispensed item. If the BlockDispenseEvent is
  NOT canceled, this item may not exist, use `$item.dispensed` instead.
- `$item.filter.<N>[.<I>]` - the `I`th item referenced by the `N`th filter, or the first item if no I is provided. If
  filter `N` does not have `I` items, the recipe will be rejected.
- `$item.action.<N>[.<I>]` - the `I`th item referenced by the `N`th action, or the first item if no I is provided. If
  action `N` does not have `I` items, the recipe will be rejected.
  <BR><BR>
- `$block.dispenser` - the dispenser block itself
- `$block.filter.<N>` - similar to `$item.filter`, without a second argument
- `$block.action.<N>` - similar to `$item.action`, without a second argument
  <BR><BR>
- `$entity.filter.<N>` - similar to `$item.filter`, without a second argument
- `$entity.action.<N>` - similar to `$item.action`, without a second argument

Non-variable items (in the `items` section) are referenced by name.

***Note: Unless otherwise stated, filter, action, item, etc. references are counted starting from `0`, so ex. the 2nd
item referenced by the 1st filter would be given by `$item.filter.0.1`***

### Recipes

The recipes section lists all the custom recipes available. Only the first recipe from the list matching a dispenser
will be used - sort your recipes accordingly. Each recipe contains:

- `dispenser` - **REQUIRED** Describes dispenser's properties.
  - `type` - **REQUIRED** The dispenser's Material type. As of 1.16, this only includes `DROPPER` and `DISPENSER`.
  - `name` - The dispenser's name. There is no color code conversion here, you must use the double-S character for
    legacy colored names.
  - `direction` - One of: `horizontal`, `vertical`, `up`, `down`, or `all`. If this option is absent, defaults to
    `all`.
    <BR><BR>
- `drop-item` - **REQUIRED** The name of the item (defined in the `items` secion) being dropped by the dispenser.
  <BR><BR>
- `cancel-event` - Set false to allow the vanilla dispense event to complete. Defaults to true. **NOTE: due to how
  Bukkit's BlockDispenseEvent works, you need to manually remove an item from the dispenser if it is cancelled (ex. with
  a `consume_item` action).**
- `soft-fail` - Set true to cancel the BlockDispenseEvent accordingly if the item matches, even if the rest of the
  filters do not. Defaults to false.
  <BR><BR>
- `permission` - Recipe permission node required to use this recipe. ***This requires a `target_entity` filter targeting
  a player***. If this option is present, the recipe will not match unless a target player is found and has the
  permission `customdispensers.recipe.<permission>`
- `permission-filter` - Number of the filter to use to get the target player.
  <BR><BR>
- `filters` - List of filters. Each **requires** a `filter` property.
  - `filter: target_block` - Describes a block that must be somewhere near the dispenser.
  - `block-data` - BlockData string describing the block to match (ex `"minecraft:cauldron[level=0]"`).
  - `distance` - Max distance to look for a target. `1` will check the block in front of the dispenser. Negative values
    check behind the dispenser.
  - `exact` - Set to `true` to use the exact distance value rather than searching up to that distance.
  - `direct` - Set to `true` to require a direct line of sight from the dispenser to the block, or `false` to ignore
    obstructions and search the maximum distance.
  - `direction` - Direction to look for the target, uses the same values as `dispenser.direction`.
  - `contains-items` - List of named items the target block must contain.
    <BR><BR>
  - `filter: target_entity` - Describes an entity that must be somewhere near the dispenser.
  - `entity-type` - The entity's EntityType, or the name of one of its Bukkit
    [Entity interfaces](https://papermc.io/javadocs/paper/1.16/index.html?overview-summary.html)
  - `distance` - Same as target_block.
  - `exact` - Same as target_block.
  - `direct` - Same as target_block.
  - `direction` - Same as target_block.
  - `contains-items` - Same as target_block.
    <BR><BR>
- `actions` - List of actions to take when the filters match. Possible actions include:
  - `consume_item <item> [<amount>]` - If given an item variable, consumes one or more items from the given item stack.
    *If no amount is given, it will consume the whole stack.* If used, this action should be the first in any recipes.
  - `drop_item <item> [<speed> <variance>]` - Drops an item, optionally with a given velocity. This will have an effect
    regardless of whether the event is cancelled. **NOTE:** this ***clones*** the item before inserting into the target
    inventory to prevent multiple inventories having the same item reference, ***make sure to consume the original item
    after using this action to prevent dupes*** (if using a variable item).
  - `add_item <item> [<inventory direction>]` - Adds an item to the dispenser's inventory, or if the inventory is full,
    drops the item above the dispenser. If the block above the dispenser has an inventory (ex. a hopper), it will try to
    insert the item in that inventory first, only dropping it if the item will not fit. If a direction is given, the
    dispenser's inventory will never be used and any adjacent inventory matching the direction will be preferred before
    dropping the item. **NOTE:** this ***clones*** the item before inserting into the target inventory to prevent
    multiple inventories having the same item reference, ***make sure to consume the original item after using this
    action to prevent dupes*** (if using a variable item).
  - `insert_item <item> block|entity <block|entity>` - Adds an item to the inventory of the targeted block or entity,
    dropping the item at the target if the inventory is full. **NOTE:** this ***clones*** the item before inserting into
    the target inventory to prevent multiple inventories having the same item reference,
    ***make sure to consume the original item after using this action to prevent dupes*** (if using a variable item).
  - `damage_item <item> [<amount>]` - If given an item variable, applies one or more damage to the item if damageable.
  - `set_block <block> <blockdata>` - Sets the target block to the given BlockData (ex.
    `set_block $block.filter.2 minecraft:cauldron[level=3]`)
  - `break_block <block> <item>` - Breaks the target block using the given item.
  - ~~.`attack_entity <entity> <item>` - Attacks the target entity using the given item.~~ (Coming soon)
  - `play_sound <sound> <pitch> [<pitch variance>]` - Plays the given sound with the given pitch, optionally with extra
    pitch variance. Sound names can be found in the
    [Sound Javadoc](https://papermc.io/javadocs/paper/1.16/index.html?overview-summary.html)
  - `particle <particle> [<speed> <variance>] [<amount>] [block|entity <block|entity>]` - Spawns particles, optionally
    at a given entity or block. Particle names can be found in the
    [Particle Javadoc](https://papermc.io/javadocs/paper/1.16/index.html?overview-summary.html)

### Items

Defines custom items to be used by recipe actions (ex. `add_item waterbucket`). Each subsection here is the name used in
recipe filters and actions and contains a Bukkit ItemStack in YAML form, ex:

```yaml
lmao-pigbanner:
  ==: org.bukkit.inventory.ItemStack
  v: 2586
  type: BLACK_BANNER
  meta:
    ==: ItemMeta
    meta-type: BANNER
    patterns:
      - ==: Pattern
        color: RED
        pattern: pig
```

See [the Material Javadoc](https://papermc.io/javadocs/paper/1.16/org/bukkit/Material.html) for the values you can use
as `type`s (replacing 1.16 in the URL with whichever major game version you're using), and
[the Minecraft Wiki](https://minecraft.fandom.com/wiki/Data_version#List_of_data_versions) for the item version number
(`v: 2586` in 1.16.5). Only use item/data version numbers that match the version of Minecraft you are using or
older.<BR>
*Alternatively hold an item in-game and run the command `/itemyaml` to see its YAML form.*

## Example config.yml

The example config below (which is the plugin default) adds 2 recipes: one that dispenses a water bucket into a
cauldron, and one that fills a water bucket from a full cauldron. It also contains 2 named items: an empty bucket, and a
water bucket, referenced by the two recipes.

<details>
  <summary>config.yml</summary>

```yaml
recipes:
  - dispenser:
      type: DISPENSER
    drop-item: waterbucket
    cancel-event: true
    filters:
      - filter: target_block
        block-data: "minecraft:cauldron"
    actions:
      - consume_item $item.dispenser 1
      - set_block $block.filter.0 minecraft:cauldron[level=3]
      - add_item bucket
      - play_sound ITEM_BUCKET_EMPTY 1.0 0.125
  - dispenser:
      type: DISPENSER
    drop-item: bucket
    cancel-event: true
    filters:
      - filter: target_block
        block-data: "minecraft:cauldron[level=3]"
    actions:
      - consume_item $item.dispenser 1
      - set_block $block.filter.0 minecraft:cauldron[level=0]
      - add_item waterbucket
      - play_sound ITEM_BUCKET_FILL 1.0 0.125
items:
  bucket:
    ==: org.bukkit.inventory.ItemStack
    v: 2586
    type: BUCKET
  waterbucket:
    ==: org.bukkit.inventory.ItemStack
    v: 2586
    type: WATER_BUCKET
```

</details>

The below config shows an example of a permission gated recipe that spawns lava particles around a target entity if the
dispenser has line-of-sight to that entity within 5 blocks and that entity has the `customdispensers.recipe.lavapoof`
permission. If the target entity does not have the permission (or there is no entity), the recipe will soft-fail,
cancelling the BlockDispenseEvent but not triggering the particle action.

<details>
  <summary>config.yml</summary>

```yaml
recipes:
  - dispenser:
      type: DISPENSER
    drop-item: dsword
    soft-fail: true
    permission: lavapoof
    permission-filter: 0
    filters:
      - filter: target_entity
        distance: 5
        direct: true
    actions:
      - particle LAVA 0 0 20 entity $entity.filter.0
items:
  dsword:
    ==: org.bukkit.inventory.ItemStack
    v: 2586
    type: DIAMOND_SWORD
```

</details>

### Possible Pitfalls

Due to how some actions, variables, and the BlockDispenseEvent work, actions and variables need some extra care to make
sure they will function without error. As development continues I will try to handle some of these cases more elegantly
or reject recipes that might cause an error.

<details>
  <summary>Example 1</summary>

```yaml
  - dispenser:
      type: DROPPER
    drop-item: dpick
    filters:
      - filter: target_block
        distance: 5
        direct: true
    actions:
      - damage_item $item.dispenser 250
      - break_block $block.filter.0 $item.dispenser
```

This recipe requires a Dropper to dispense a named item "dpick", which refers to a diamond pickaxe. It also requires a
block within 5 blocks of its front. By default, this recipe will cancel the vanilla dispense event.

In this order, the first action will take 250 durability from the pickaxe, possibly breaking it, then the second action
will attempt to use the pickaxe to break the block targeted by filter 0, using either air or a non-existant item.

***In general, actions that <u>modify</u> an item should be run after actions that use that item*** unless the
modification is required for the later action. This is especially true for actions that damage or consume an item. The
corrected action list would look like this:

```yaml
    actions:
      - break_block $block.filter.0 $item.dispenser
      - damage_item $item.dispenser 250
```

</details>
