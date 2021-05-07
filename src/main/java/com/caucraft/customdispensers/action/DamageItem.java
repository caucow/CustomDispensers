package com.caucraft.customdispensers.action;

import com.caucraft.customdispensers.ActionResult;
import com.caucraft.customdispensers.Config;
import com.caucraft.customdispensers.Recipe;
import com.caucraft.customdispensers.RecipeContext;
import com.caucraft.customdispensers.action.reference.ItemReference;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

public class DamageItem implements Action {

    private final ItemReference itemRef;
    private final int amount;

    public DamageItem(ItemReference itemRef, int amount) {
        this.itemRef = itemRef;
        this.amount = amount;
    }

    @Override
    public int getExpectedItems() {
        return 1;
    }

    @Override
    public ActionResult act(RecipeContext context, Recipe recipe) {
        ItemStack item = itemRef.getTarget(context, recipe);
        ItemMeta meta = item.getItemMeta();
        if (meta instanceof Damageable) {
            Damageable dmgMeta = (Damageable) meta;
            int newDmg = dmgMeta.getDamage() + amount;
            if (newDmg >= item.getType().getMaxDurability()) {
                item.setAmount(item.getAmount() - 1);
                dmgMeta.setDamage(0);
                Location dsprLoc = context.event.getBlock().getLocation().add(0.5, 0.5, 0.5);
                dsprLoc.getWorld().playSound(dsprLoc, Sound.ENTITY_ITEM_BREAK, 1.0F, (float) Math.random() * 0.1F + 0.95F);
            } else {
                dmgMeta.setDamage(newDmg);
            }
            item.setItemMeta(meta);
        }
        return new ActionResult(new ItemStack[] { item }, null, null);
    }

    public static Action parseAction(Config config, String[] args, int actionNumber) {
        ItemReference itemRef = ItemReference.getReference(config, args[1], actionNumber);
        int amount = 1;
        if (args.length > 2) {
            amount = Integer.parseInt(args[2]);
        }
        return new DamageItem(itemRef, amount);
    }
}
