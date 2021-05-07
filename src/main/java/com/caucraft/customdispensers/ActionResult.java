package com.caucraft.customdispensers;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

public class ActionResult {

    public final ItemStack[] items;
    public final Block block;
    public final Entity entity;

    public ActionResult(ItemStack[] items, Block block, Entity entity) {
        this.items = items;
        this.block = block;
        this.entity = entity;
    }
}
