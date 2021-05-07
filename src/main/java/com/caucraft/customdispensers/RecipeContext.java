package com.caucraft.customdispensers;

import com.caucraft.customdispensers.action.Action;
import com.caucraft.customdispensers.filter.Filter;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permissible;

import java.util.ArrayList;
import java.util.List;

public class RecipeContext {

    public final BlockDispenseEvent event;
    public final CustomDispensers plugin;
    public ItemStack dispenserItem;
    public List<ActionResult> filterResults;
    public List<ActionResult> actionResults;

    public RecipeContext(CustomDispensers plugin, BlockDispenseEvent event) {
        this.plugin = plugin;
        this.event = event;
    }

    /**
     * @return true if a recipe was applied to the event.
     */
    public boolean tryApplyRecipe(List<Recipe> recipes) {
        Block dsprBlock = event.getBlock();
        ItemStack dispensed = event.getItem();
        recipes:
        for (Recipe recipe : recipes) {
            if (recipe.dsprFilter.apply(dsprBlock) != null
                    && dispensed.isSimilar(recipe.dropItem)) {
                if (!recipe.softFail) {
                    List<Filter> filters = recipe.filters;
                    for (int i = 0; i < filters.size(); i++) {
                        ActionResult result = filters.get(i).apply(dsprBlock);
                        if (result == null) {
                            continue recipes;
                        }
                        if (recipe.permission != null && recipe.permissionFilter == i) {
                            if (result.entity == null || !((Permissible) result.entity).hasPermission(recipe.permission)) {
                                continue recipes;
                            }
                        }
                    }
                }

                if (recipe.cancelEvent) {
                    event.setCancelled(true);
                }
                Bukkit.getScheduler().runTask(plugin, () -> {
                    Block dsprBlockInner = event.getBlock();
                    ItemStack dispensedInner = event.getItem();
                    // A tick has passed! Make sure the recipe still applies, no blocks have been changed, etc.
                    // Why can't BlockDispenseEvent just *not* be trash garbage?
                    // This is so fucking stupid and unnecessary, except it's actually even more stupid, but necessary.
                    // In order:
                    // dispenser block still matches (dispenser filter returns non-null)
                    // AND event is cancelled according to the recipe
                    // AND
                    //     ( the event was cancelled and the dispenser still contains the recipe item )
                    //     OR ( the event was not cancelled and the dispensed item matches the recipe item )
                    if (recipe.dsprFilter.apply(dsprBlockInner) != null
                            && event.isCancelled() == recipe.cancelEvent
                            && (recipe.cancelEvent && ((InventoryHolder) dsprBlockInner.getState()).getInventory().containsAtLeast(recipe.dropItem, recipe.dropItem.getAmount()))
                                    || (!recipe.cancelEvent && dispensedInner.isSimilar(recipe.dropItem))) {
                        if (event.isCancelled()) {
                            Inventory inv = ((InventoryHolder) event.getBlock().getState()).getInventory();
                            ItemStack dropItem = recipe.dropItem;
                            for (int i = 0; i < inv.getSize(); i++) {
                                ItemStack item = inv.getItem(i);
                                if (item != null && item.isSimilar(dropItem) && item.getAmount() >= dropItem.getAmount()) {
                                    dispenserItem = item;
                                }
                            }
                        } else {
                            dispenserItem = event.getItem();
                        }
                        List<Filter> filters = recipe.filters;
                        List<ActionResult> tempResults = new ArrayList<>();
                        for (int i = 0; i < filters.size(); i++) {
                            ActionResult result = filters.get(i).apply(dsprBlockInner);
                            if (result == null) {
                                return;
                            }
                            tempResults.add(result);
                        }
                        if (recipe.permission != null) {
                            ActionResult result = tempResults.get(recipe.permissionFilter);
                            if (result.entity == null || !((Permissible) result.entity).hasPermission(recipe.permission)) {
                                return;
                            }
                        }
                        this.filterResults = tempResults;
                        List<Action> actions = recipe.actions;
                        this.actionResults = tempResults = new ArrayList<>();
                        for (int i = 0; i < actions.size(); i++) {
                            tempResults.add(actions.get(i).act(this, recipe));
                        }
                    }
                });
                return true;
            }
        }
        return false;
    }

}
