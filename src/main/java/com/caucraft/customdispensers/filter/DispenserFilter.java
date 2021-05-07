package com.caucraft.customdispensers.filter;

import com.caucraft.customdispensers.ActionResult;
import com.caucraft.customdispensers.Direction;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.data.type.Dispenser;

public class DispenserFilter implements Filter {
    public final Material type;
    public final String name;
    public final Direction direction;

    public DispenserFilter(Material type, String name, Direction direction) {
        this.type = type;
        this.name = name;
        this.direction = direction;
    }

    @Override
    public ActionResult apply(Block dspr) {
        Dispenser ddata = (Dispenser) dspr.getBlockData();
        Container container = (Container) dspr.getState();
        boolean result = dspr.getType() == this.type
                && (this.name == null || this.name.equals(container.getCustomName()))
                && (this.direction == null || this.direction.matchesFace(ddata.getFacing()));
        return result ? new ActionResult(null, null, null) : null;
    }
}
