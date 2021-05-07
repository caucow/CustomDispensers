package com.caucraft.customdispensers;

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.inventory.InventoryHolder;

import java.util.List;

public class EventListener implements Listener {

    private final CustomDispensers plugin;

    public EventListener(CustomDispensers plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDispense(BlockDispenseEvent evt) {
        Block dspr = evt.getBlock();
        InventoryHolder invHolder = (InventoryHolder) dspr.getState();
        List<Recipe> recipes = plugin.getDispenserConfig().getRecipes(dspr.getType(), evt.getItem().getType());
        if (recipes != null) {
            RecipeContext rc = new RecipeContext(plugin, evt);
            rc.tryApplyRecipe(recipes);
        }
    }
}
