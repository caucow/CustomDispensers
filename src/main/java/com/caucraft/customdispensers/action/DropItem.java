package com.caucraft.customdispensers.action;

import com.caucraft.customdispensers.ActionResult;
import com.caucraft.customdispensers.Config;
import com.caucraft.customdispensers.Recipe;
import com.caucraft.customdispensers.RecipeContext;
import com.caucraft.customdispensers.action.reference.ItemReference;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Dispenser;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class DropItem implements Action {

    private final ItemReference itemRef;
    private final double speed;
    private final double variance;

    public DropItem(ItemReference itemRef, double speed, double variance) {
        this.itemRef = itemRef;
        this.speed = speed;
        this.variance = variance;
    }

    @Override
    public int getExpectedItems() {
        return 1;
    }

    @Override
    public int getExpectedEntities() {
        return 1;
    }

    @Override
    public ActionResult act(RecipeContext context, Recipe recipe) {
        Block dspr = context.event.getBlock();
        BlockFace face = ((Dispenser) dspr.getBlockData()).getFacing();
        Location spawnLoc = dspr.getLocation().add(0.5, 0.25, 0.5).add(face.getDirection().multiply(0.625));
        ItemStack item = itemRef.getTarget(context, recipe).clone();
        Item itemEntity = spawnLoc.getWorld().dropItem(spawnLoc, item, (eitem) -> {
            eitem.setVelocity(face.getDirection().multiply(speed).add(new Vector(getRandom(variance), 0.2 + getRandom(variance), getRandom(variance))));
        });
        return new ActionResult(new ItemStack[] { item }, null, itemEntity);
    }

    private static double getRandom(double variance) {
        return (Math.random() * 2 - 1) * variance;
    }

    public static Action parseAction(Config config, String[] args, int actionNumber) {
        ItemReference itemRef = ItemReference.getReference(config, args[1], actionNumber);
        double speed = 0.3;
        double variance = 0.0075;
        if (args.length > 3) {
            speed = Double.parseDouble(args[2]);
            variance = Double.parseDouble(args[3]);
        }
        return new DropItem(itemRef, speed, variance);
    }
}
