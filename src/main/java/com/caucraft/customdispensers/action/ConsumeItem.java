package com.caucraft.customdispensers.action;

import com.caucraft.customdispensers.ActionResult;
import com.caucraft.customdispensers.Config;
import com.caucraft.customdispensers.Recipe;
import com.caucraft.customdispensers.RecipeContext;
import com.caucraft.customdispensers.action.reference.ItemReference;
import org.bukkit.inventory.ItemStack;

public class ConsumeItem implements Action {

    private final ItemReference itemRef;
    private final int amount;

    public ConsumeItem(ItemReference itemRef, int amount) {
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
        if (amount == -1) {
            item.setAmount(0);
        } else {
            item.subtract(amount);
        }
        return new ActionResult(new ItemStack[] { item }, null, null);
    }

    @Override
    public void validate(Recipe recipe, int actionNum) {
        itemRef.validate(recipe);
    }

    public static Action parseAction(Config config, String[] args, int actionNumber) {
        ItemReference itemRef = ItemReference.getReference(config, args[1], actionNumber);
        int amount = -1;
        if (args.length > 2) {
            amount = Integer.parseInt(args[2]);
        }
        return new ConsumeItem(itemRef, amount);
    }
}
