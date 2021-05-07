package com.caucraft.customdispensers.action.reference;

import com.caucraft.customdispensers.Config;
import com.caucraft.customdispensers.Recipe;
import com.caucraft.customdispensers.RecipeContext;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public interface ItemReference extends Reference<ItemStack> {
    static ItemReference getReference(Config config, String desc, int actionNumber) {
        if (desc.charAt(0) == '$') {
            String[] split = desc.split("\\.");
            if (!split[0].startsWith("$item")) {
                throw new IllegalArgumentException("Item variable expected, got: " + desc);
            }
            switch (split[1]) {
                case "dispensed": {
                    return new ItemReference() {
                        @Override
                        public ItemStack getTarget(RecipeContext context, Recipe recipe) {
                            return context.event.getItem();
                        }

                        @Override
                        public void validate(Recipe recipe) {
                            if (recipe.cancelEvent) {
                                throw new IllegalArgumentException("ItemReference to dispensed item cannot be used when the event is cancelled");
                            }
                        }
                    };
                }
                case "dispenser": {
                    return new ItemReference() {
                        @Override
                        public ItemStack getTarget(RecipeContext context, Recipe recipe) {
                            ItemStack contextItem = context.dispenserItem;
                            if (contextItem != null) {
                                return contextItem;
                            }
                            // In theory, we should not need to fall back to an inventory search.
                            Inventory inv = ((InventoryHolder) context.event.getBlock().getState()).getInventory();
                            ItemStack dropItem = recipe.dropItem;
                            for (int i = 0; i < inv.getSize(); i++) {
                                ItemStack item = inv.getItem(i);
                                if (item != null && item.isSimilar(dropItem) && item.getAmount() >= dropItem.getAmount()) {
                                    return item;
                                }
                            }
                            // In theory, should not happen because validation + recipe matching should fail before this
                            throw new IllegalStateException("Could not get ItemReference for dispenser item.");
                        }

                        @Override
                        public void validate(Recipe recipe) {
                            if (!recipe.cancelEvent) {
                                throw new IllegalArgumentException("ItemReference to item in dispenser cannot be used when the event is not cancelled");
                            }
                        }
                    };
                }
                case "filter": {
                    int N = Integer.parseInt(split[2]);
                    int I = split.length > 3 ? Integer.parseInt(split[3]) : 0;
                    return new ItemReference() {
                        @Override
                        public ItemStack getTarget(RecipeContext context, Recipe recipe) {
                            return context.filterResults.get(N).items[I];
                        }

                        @Override
                        public void validate(Recipe recipe) {
                            if (recipe.filters.get(N).getExpectedItems() <= I) {
                                throw new IllegalArgumentException("Filter " + N + " does not reference " + (I + 1) + " items.");
                            }
                        }
                    };
                }
                case "action": {
                    int N = Integer.parseInt(split[2]);
                    int I = split.length > 3 ? Integer.parseInt(split[3]) : 0;
                    if (N >= actionNumber) {
                        throw new IllegalArgumentException("Cannot reference ActionResult " + N + " from action " + actionNumber);
                    }
                    return new ItemReference() {
                        @Override
                        public ItemStack getTarget(RecipeContext context, Recipe recipe) {
                            return context.actionResults.get(N).items[I];
                        }

                        @Override
                        public void validate(Recipe recipe) {
                            if (recipe.actions.get(N).getExpectedItems() <= I) {
                                throw new IllegalArgumentException("Action " + N + " does not reference " + (I + 1) + " items.");
                            }
                        }
                    };
                }
                default:
                    throw new IllegalArgumentException("Expected 'dispensed', 'action', or 'filter' in item reference, got " + split[1]);
            }
        } else {
            return new ItemReference() {
                @Override
                public ItemStack getTarget(RecipeContext context, Recipe recipe) {
                    return config.getItem(desc).clone();
                }

                @Override
                public void validate(Recipe recipe) {
                    ItemStack item = config.getItem(desc);
                    if (item == null) {
                        throw new IllegalArgumentException("No item named " + desc);
                    }
                }
            };
        }
    }
}
