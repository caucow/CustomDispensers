package com.caucraft.customdispensers.filter;

import com.caucraft.customdispensers.ActionResult;
import com.caucraft.customdispensers.Config;
import com.caucraft.customdispensers.Direction;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Dispenser;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BlockFilter implements Filter {

    private final BlockData blockData;
    private final int distance;
    private final boolean exact;
    private final boolean direct;
    private final Direction direction;
    private final List<ItemStack> contains;

    public BlockFilter(BlockData blockData, int distance, boolean exact, boolean direct, Direction direction, List<ItemStack> contains) {
        this.blockData = blockData;
        this.distance = distance;
        this.exact = exact; // TODO ensure this is implemented properly. It is not right now.
        this.direct = direct;
        this.direction = direction;
        this.contains = contains;
    }

    @Override
    public int getExpectedItems() {
        return (contains == null ) ? 0 : contains.size();
    }

    @Override
    public int getExpectedBlocks() {
        return 1;
    }

    @Override
    public ActionResult apply(Block dspr) {
        Block target = null;
        if (direction == null) {
            BlockFace face = ((Dispenser) dspr.getBlockData()).getFacing();
            if (distance > 0) {
                for (int i = exact ? distance : 1; i <= distance; i++) {
                    Block block = dspr.getRelative(face, i);
                    if (block.getType().isAir()) {
                        continue;
                    }
                    ActionResult matchResult = match(block);
                    if (matchResult != null && (!direct || checkDirect(dspr, face, i))) {
                        return matchResult;
                    } else if (direct) {
                        return null;
                    }
                }
            } else {
                for (int i = exact ? distance : -1; i >= distance; i--) {
                    Block block = dspr.getRelative(face, i);
                    if (block.getType().isAir()) {
                        continue;
                    }
                    ActionResult matchResult = match(block);
                    if (matchResult != null && (!direct || checkDirect(dspr, face, i))) {
                        return matchResult;
                    } else if (direct) {
                        return null;
                    }
                }
            }
        } else {
            BlockFace[] faces = direction.getFaces();
            for (int i = exact ? distance : 1; i <= distance; i++) {
                for (int f = 0; f < faces.length; f++) {
                    BlockFace face = faces[f];
                    Block block = dspr.getRelative(face, i);
                    if (block.getType().isAir()) {
                        continue;
                    }
                    ActionResult matchResult = match(block);
                    if (matchResult != null && (!direct || checkDirect(dspr, face, i))) {
                        return matchResult;
                    } else if (direct) {
                        return null;
                    }
                }
            }
        }
        return null;
    }

    private ActionResult match(Block block) {
        if (blockData != null && !block.getBlockData().matches(blockData)) {
            return null;
        }
        List<ItemStack> items = contains != null && !contains.isEmpty() ? new ArrayList<>(contains.size()) : null;
        if (items != null) {
            BlockState state = block.getState();
            if (!(state instanceof InventoryHolder)) {
                return null;
            }
            ItemStack[] inv = ((InventoryHolder) state).getInventory().getContents();
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
        return new ActionResult(items == null ? null : items.toArray(new ItemStack[items.size()]), block, null);
    }

    private boolean checkDirect(Block block, BlockFace face, int dist) {
        int inc = dist < 0 ? -1 : 1;
        for (int i = inc; i != dist; i += inc) {
            if (!block.getRelative(face, i).getType().isAir()) {
                return false;
            }
        }
        return true;
    }

    public static Filter parseFilter(Config config, Map<?, ?> filterData) {
        BlockData bd = null;
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
                case "block-data":
                    bd = Bukkit.createBlockData((String) v);
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

        return new BlockFilter(bd, distance, exact, direct, direction, contains);
    }
}
