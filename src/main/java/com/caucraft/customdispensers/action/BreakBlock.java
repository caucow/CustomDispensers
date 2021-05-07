package com.caucraft.customdispensers.action;

import com.caucraft.customdispensers.ActionResult;
import com.caucraft.customdispensers.Config;
import com.caucraft.customdispensers.Recipe;
import com.caucraft.customdispensers.RecipeContext;
import com.caucraft.customdispensers.action.reference.BlockReference;
import com.caucraft.customdispensers.action.reference.ItemReference;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public class BreakBlock implements Action {

    private final BlockReference blockRef;
    private final ItemReference itemRef;

    public BreakBlock(BlockReference blockRef, ItemReference itemRef) {
        this.blockRef = blockRef;
        this.itemRef = itemRef;
    }

    @Override
    public int getExpectedItems() {
        return 1;
    }

    @Override
    public int getExpectedBlocks() {
        return 1;
    }

    @Override
    public ActionResult act(RecipeContext context, Recipe recipe) {
        Block block = blockRef.getTarget(context, recipe);
        ItemStack item = itemRef.getTarget(context, recipe);
        block.breakNaturally(item);
        return new ActionResult(new ItemStack[] { item }, block, null);
    }

    @Override
    public void validate(Recipe recipe, int actionNum) {
        blockRef.validate(recipe);
        itemRef.validate(recipe);
    }

    public static Action parseAction(Config config, String[] args, int actionNumber) {
        BlockReference blockRef = BlockReference.getReference(config, args[1], actionNumber);
        ItemReference itemRef = ItemReference.getReference(config, args[2], actionNumber);
        return new BreakBlock(blockRef, itemRef);
    }
}
