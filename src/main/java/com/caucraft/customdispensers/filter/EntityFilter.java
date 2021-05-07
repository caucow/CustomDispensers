package com.caucraft.customdispensers.filter;

import com.caucraft.customdispensers.ActionResult;
import com.caucraft.customdispensers.Config;
import com.caucraft.customdispensers.Direction;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.type.Dispenser;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EntityFilter implements Filter {
    private final EntityType entityType;
    private final int distance;
    private final boolean exact;
    private final boolean direct;
    private final Direction direction;
    private final List<ItemStack> contains;

    public EntityFilter(EntityType entityType, int distance, boolean exact, boolean direct, Direction direction, List<ItemStack> contains) {
        this.entityType = entityType;
        this.distance = distance;
        this.exact = exact;
        this.direct = direct;
        this.direction = direction;
        this.contains = contains;
    }

    public int getExpectedEntities() {
        return 1;
    }

    @Override
    public ActionResult apply(Block dspr) {
        double expansion = 0.25;
        Location dsprLoc = dspr.getLocation().add(0.5, 0.5, 0.5);
        BoundingBox dsprbb = new BoundingBox(dsprLoc.getX() + expansion, dsprLoc.getY() + expansion, dsprLoc.getZ() + expansion, dsprLoc.getX() + expansion, dsprLoc.getY() + expansion, dsprLoc.getZ() + expansion);
        if (direction == null) {
            BlockFace face = ((Dispenser) dspr.getBlockData()).getFacing();
            BoundingBox testbb = dsprbb.clone().union(dsprLoc.clone().add(face.getDirection().multiply(distance + expansion)));
            List<Entity> entities = new ArrayList<>(dsprLoc.getWorld().getNearbyEntities(testbb));
            entities.sort(Comparator.comparingDouble(e -> e.getLocation().distanceSquared(dsprLoc)));
            for (Entity e : entities) {
                ActionResult matchResult = match(e);
                if (matchResult != null && (!direct || checkDirect(dspr, e))) {
                    return matchResult;
                } else if (direct) {
                    return null;
                }
            }
        } else {
            BlockFace[] faces = direction.getFaces();
            for (int f = 0; f < faces.length; f++) {
                BlockFace face = faces[f];
                BoundingBox testbb = dsprbb.clone().union(dsprLoc.clone().add(face.getDirection().multiply(distance + expansion)));
                List<Entity> entities = new ArrayList<>(dsprLoc.getWorld().getNearbyEntities(testbb));
                entities.sort(Comparator.comparingDouble(e -> e.getLocation().distanceSquared(dsprLoc)));
                for (Entity e : entities) {
                    ActionResult matchResult = match(e);
                    if (matchResult != null && (!direct || checkDirect(dspr, e))) {
                        return matchResult;
                    } else if (direct) {
                        return null;
                    }
                }
            }
        }
        return null;
    }

    private ActionResult match(Entity entity) {
        if (entityType != null && entity.getType() != entityType) {
            return null;
        }
        List<ItemStack> items = contains != null && !contains.isEmpty() ? new ArrayList<>(contains.size()) : null;
        if (items != null) {
            if (!(entity instanceof InventoryHolder)) {
                return null;
            }
            ItemStack[] inv = ((InventoryHolder) entity).getInventory().getContents();
            boolean[] mask = new boolean[inv.length];
            items:
            for (ItemStack mitem : contains) {
                for (int i = 0; i < inv.length; i++) {
                    if (!mask[i] && inv[i] != null && inv[i].isSimilar(mitem) && inv[i].getAmount() >= mitem.getAmount()) {
                        items.add(inv[i]);
                        mask[i] = true;
                        continue items;
                    }
                }
                return null;
            }
        }
        return new ActionResult(items == null ? null : items.toArray(new ItemStack[items.size()]), null, entity);
    }

    private boolean checkDirect(Block dspr, Entity entity) {
        Vector start = dspr.getLocation().add(0.5, 0.5, 0.5).toVector();
        Vector dir = entity.getLocation().add(0.0, entity.getHeight() * 0.5, 0.0).subtract(start).toVector();
        BlockIterator bit = new BlockIterator(entity.getWorld(), start, dir, 0.0, (int) dir.length());
        while (bit.hasNext()) {
            Block next = bit.next();
            if (!next.getType().isAir() && !next.equals(dspr)) {
                return false;
            }
        }
        return true;
    }

    public static Filter parseFilter(Config config, Map<?, ?> filterData) {
        EntityType type = null;
        int distance = 1;
        boolean exact = false;
        boolean direct = false;
        Direction direction = null;
        List<ItemStack> contains = null;

        for (Map.Entry<?, ?> e : filterData.entrySet()) {
            Object v = e.getValue();
            switch ((String) e.getKey()) {
                case "filter":
                    break;
                case "entity-type":
                    type = Config.getEnum((String) v, EntityType.class, null);
                    break;
                case "distance":
                    distance = ((Number) v).intValue();
                    break;
                case "exact":
                    exact = (Boolean) v;
                    break;
                case "direct":
                    direct = (Boolean) v;
                    break;
                case "direction":
                    direction = Config.getEnum((String) v, Direction.class, null);
                    break;
                case "contains-items":
                    List<String> itemNames = ((List<?>) v).stream().map(o -> (String) o).collect(Collectors.toList());
                    List<ItemStack> items = new ArrayList<>(itemNames.size());
                    for (String name : itemNames) {
                        ItemStack item = config.getItem(name);
                        if (item == null) {
                            throw new IllegalArgumentException(name + " is not a valid named item");
                        }
                        items.add(item);
                    }
                    contains = items;
                    break;
                default:
                    throw new IllegalArgumentException("Invalid option for BlockFilter: " + e.getKey());
            }
        }

        return new EntityFilter(type, distance, exact, direct, direction, contains);
    }
}
