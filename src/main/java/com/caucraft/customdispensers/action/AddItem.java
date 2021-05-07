package com.caucraft.customdispensers.action;

import com.caucraft.customdispensers.ActionResult;
import com.caucraft.customdispensers.Config;
import com.caucraft.customdispensers.Direction;
import com.caucraft.customdispensers.Recipe;
import com.caucraft.customdispensers.RecipeContext;
import com.caucraft.customdispensers.action.reference.ItemReference;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Item;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class AddItem implements Action {

    private final ItemReference itemRef;
    private final Direction direction;

    public AddItem(ItemReference itemRef, Direction direction) {
        this.itemRef = itemRef;
        this.direction = direction;
    }

    @Override
    public int getExpectedItems() { return 1; }

    @Override
    public ActionResult act(RecipeContext context, Recipe recipe) {
        Block dspr = context.event.getBlock();
        ItemStack item = itemRef.getTarget(context, recipe).clone();
        if (direction == null) {
            ActionResult result = tryAddToInventory(dspr, ((InventoryHolder) dspr.getState()).getInventory(), item);
            if (result != null) {
                return result;
            }
            Block above = dspr.getRelative(BlockFace.UP);
            BlockState aboveState = above.getState();
            if (aboveState instanceof InventoryHolder) {
                result = tryAddToInventory(above, ((InventoryHolder) aboveState).getInventory(), item);
                if (result != null) {
                    return result;
                }
            }
        } else {
            for (BlockFace face : direction.getFaces()) {
                Block adj = dspr.getRelative(face);
                BlockState adjState = adj.getState();
                if (adjState instanceof InventoryHolder) {
                    ActionResult result = tryAddToInventory(adj, ((InventoryHolder) adjState).getInventory(), item);
                    if (result != null) {
                        return result;
                    }
                }
            }
        }
        Location above = dspr.getLocation().add(0.5, 1.125, 0.5);
        Item itemEntity = above.getWorld().dropItem(above, item);
        return new ActionResult(new ItemStack[] { itemEntity.getItemStack() }, null, null);
    }

    @Override
    public void validate(Recipe recipe, int actionNum) {
        itemRef.validate(recipe);
    }

    private static ActionResult tryAddToInventory(Block block, Inventory inv, ItemStack item) {
        // Non-filled stack check
        int inull = -1;
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack curItem = inv.getItem(i);
            if (curItem == null || curItem.getAmount() == 0) {
                if (inull == -1) {
                    inull = i;
                }
            } else if (curItem.isSimilar(item) && item.getAmount() + curItem.getAmount() < curItem.getMaxStackSize()) {
                // oh look it's the line mojang forgot when making the 1.12 recipe book
                curItem.add(item.getAmount());
                return new ActionResult(new ItemStack[] { curItem }, block, null);
            }
        }
        // Empty slot fallback
        if (inull != -1) {
            inv.setItem(inull, item);
            return new ActionResult(new ItemStack[] { item }, block, null);
        }
        return null;
    }

    public static Action parseAction(Config config, String[] args, int actionNumber) {
        ItemReference itemRef = ItemReference.getReference(config, args[1], actionNumber);
        Direction direction = args.length > 2 ? Config.getEnum(args[2], Direction.class, null) : null;
        return new AddItem(itemRef, direction);
    }
}
