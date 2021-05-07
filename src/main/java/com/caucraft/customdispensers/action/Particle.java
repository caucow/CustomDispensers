package com.caucraft.customdispensers.action;

import com.caucraft.customdispensers.ActionResult;
import com.caucraft.customdispensers.Config;
import com.caucraft.customdispensers.Recipe;
import com.caucraft.customdispensers.RecipeContext;
import com.caucraft.customdispensers.action.reference.BlockReference;
import com.caucraft.customdispensers.action.reference.EntityReference;
import com.caucraft.customdispensers.action.reference.Reference;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Dispenser;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

public class Particle implements Action {

    private final org.bukkit.Particle particle;
    private final double speed;
    private final double variance;
    private final int amount;
    private final Reference<?> ref;

    public Particle(org.bukkit.Particle particle, double speed, double variance, int amount, Reference<?> ref) {
        this.particle = particle;
        this.speed = speed;
        this.variance = variance;
        this.amount = amount;
        this.ref = ref;
    }

    @Override
    public ActionResult act(RecipeContext context, Recipe recipe) {
        Location spawnLoc;
        World world;
        if (ref != null) {
            Object target = ref.getTarget(context, recipe);
            if (target instanceof Block) {
                spawnLoc = ((Block) target).getLocation().add(0.5, 0.5, 0.5);
            } else if (target instanceof Entity) {
                Entity ent = (Entity) target;
                spawnLoc = ent.getLocation().add(0, ent.getHeight() * 0.5, 0);
            } else {
                throw new IllegalStateException("Expected Block of Entity target, got: " + (target == null ? null : target.getClass().getSimpleName()));
            }
            world = spawnLoc.getWorld();
            for (int i = 0; i < amount; i++) {
                Vector dir = new Vector(getRandom(1), getRandom(1), getRandom(1)).normalize().multiply(speed);
                world.spawnParticle(particle, spawnLoc, 0, dir.getX() + getRandom(variance), dir.getY() + getRandom(variance), dir.getZ() + getRandom(variance));
            }
        } else {
            Block dspr = context.event.getBlock();
            BlockFace face = ((Dispenser) dspr.getBlockData()).getFacing();
            spawnLoc = dspr.getLocation().add(0.5, 0.5, 0.5);
            world = spawnLoc.getWorld();
            Vector dir = face.getDirection().multiply(speed);
            for (int i = 0; i < amount; i++) {
                world.spawnParticle(particle, spawnLoc, 0, dir.getX() + getRandom(variance), dir.getY() + getRandom(variance), dir.getZ() + getRandom(variance));
            }
        }
        return new ActionResult(null, null, null);
    }

    private static double getRandom(double variance) {
        return (Math.random() * 2 - 1) * variance;
    }

    public static Action parseAction(Config config, String[] args, int actionNumber) {
        org.bukkit.Particle particle = Config.getEnum(args[1], org.bukkit.Particle.class, "Check the Particle javadoc for your server version.");
        double speed = 0.25;
        double variance = 0.125;
        int amount = 10;
        Reference<?> ref = null;
        if (args.length > 3) {
            speed = Double.parseDouble(args[2]);
            variance = Double.parseDouble(args[3]);
        }
        if (args.length > 4) {
            amount = Integer.parseInt(args[4]);
        }
        if (args.length > 6) {
            if (args[5].equals("block")) {
                ref = BlockReference.getReference(config, args[6], actionNumber);
            } else if (args[5].equals("entity")) {
                ref = EntityReference.getReference(config, args[6], actionNumber);
            } else {
                throw new IllegalArgumentException("Particle action needs a block or entity reference, got: " + args[2]);
            }
        }
        return new Particle(particle, speed, variance, amount, ref);
    }
}
