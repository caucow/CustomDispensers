package com.caucraft.customdispensers.action;

import com.caucraft.customdispensers.ActionResult;
import com.caucraft.customdispensers.Config;
import com.caucraft.customdispensers.Recipe;
import com.caucraft.customdispensers.RecipeContext;
import com.caucraft.customdispensers.action.reference.BlockReference;
import com.caucraft.customdispensers.action.reference.EntityReference;
import com.caucraft.customdispensers.action.reference.ItemReference;
import com.caucraft.customdispensers.action.reference.Reference;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class InsertItem implements Action {

    private final Reference<?> targetRef;
    private final ItemReference itemRef;

    public InsertItem(Reference<?> targetRef, ItemReference itemRef) {
        this.targetRef = targetRef;
        this.itemRef = itemRef;
    }

    @Override
    public int getExpectedBlocks() {
        return 1;
    }

    @Override
    public int getExpectedEntities() {
        return 1;
    }

    @Override
    public ActionResult act(RecipeContext context, Recipe recipe) {
        Object ref = targetRef.getTarget(context, recipe);
        ItemStack item = itemRef.getTarget(context, recipe).clone();
        if (ref instanceof InventoryHolder) {
            Inventory inv = ((InventoryHolder) ref).getInventory();
            ActionResult result = tryAddToInventory(ref, inv, item);
            if (result != null) {
                return result;
            }
        } else if (ref instanceof Block) {
            BlockState state = ((Block) ref).getState();
            if (state instanceof InventoryHolder) {
                Inventory inv = ((InventoryHolder) state).getInventory();
                ActionResult result = tryAddToInventory(ref, inv, item);
                if (result != null) {
                    return result;
                }
            }
        }
        Location dropLoc;
        if (ref instanceof Entity) {
            dropLoc = ((Entity) ref).getLocation();
        } else if (ref instanceof Block) {
            dropLoc = ((Block) ref).getLocation().add(0.5, 1.125, 0.5);
        } else {
            ref = context.event.getBlock();
            dropLoc = ((Block) ref).getLocation().add(0.5, 1.125, 0.5);
        }
        Item itemEntity = dropLoc.getWorld().dropItem(dropLoc, item);
        return new ActionResult(new ItemStack[] { itemEntity.getItemStack() }, ref instanceof Block ? (Block) ref : null, ref instanceof Entity ? (Entity) ref : null);
    }

    private static ActionResult tryAddToInventory(Object ref, Inventory inv, ItemStack item) {
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
                return new ActionResult(new ItemStack[] { curItem }, ref instanceof Block ? (Block) ref : null, ref instanceof Entity ? (Entity) ref : null);
            }
        }
        // Empty slot fallback
        if (inull != -1) {
            inv.setItem(inull, item);
            return new ActionResult(new ItemStack[] { item }, ref instanceof Block ? (Block) ref : null, ref instanceof Entity ? (Entity) ref : null);
        }
        return null;
    }

    public static Action parseAction(Config config, String[] args, int actionNumber) {
        Reference<?> ref;
        if (args[2].equals("block")) {
            ref = BlockReference.getReference(config, args[3], actionNumber);
        } else if (args[2].equals("entity")) {
            ref = EntityReference.getReference(config, args[3], actionNumber);
        } else {
            throw new IllegalArgumentException("InsertItem action needs a block or entity reference, got: " + args[2]);
        }
        ItemReference itemRef = ItemReference.getReference(config, args[1], actionNumber);
        return new InsertItem(ref, itemRef);
    }
}
