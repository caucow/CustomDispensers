package com.caucraft.customdispensers.action;

import com.caucraft.customdispensers.ActionResult;
import com.caucraft.customdispensers.Config;
import com.caucraft.customdispensers.Recipe;
import com.caucraft.customdispensers.RecipeContext;
import com.caucraft.customdispensers.action.reference.BlockReference;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

public class SetBlock implements Action {

    private final BlockReference blockRef;
    private final BlockData blockData;

    public SetBlock(BlockReference blockRef, BlockData blockData) {
        this.blockRef = blockRef;
        this.blockData = blockData;
    }

    @Override
    public int getExpectedBlocks() {
        return 1;
    }

    @Override
    public ActionResult act(RecipeContext context, Recipe recipe) {
        Block block = blockRef.getTarget(context, recipe);
        block.setBlockData(blockData);
        return new ActionResult(null, block, null);
    }

    @Override
    public void validate(Recipe recipe, int actionNum) {
        blockRef.validate(recipe);
    }

    public static Action parseAction(Config config, String[] args, int actionNumber) {
        BlockReference blockRef = BlockReference.getReference(config, args[1], actionNumber);
        BlockData blockData = Bukkit.createBlockData(args[2]);
        return new SetBlock(blockRef, blockData);
    }

}
