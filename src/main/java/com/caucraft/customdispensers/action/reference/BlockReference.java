package com.caucraft.customdispensers.action.reference;

import com.caucraft.customdispensers.Config;
import com.caucraft.customdispensers.Recipe;
import com.caucraft.customdispensers.RecipeContext;
import org.bukkit.block.Block;

public interface BlockReference extends Reference<Block> {
    static BlockReference getReference(Config config, String desc, int actionNumber) {
        if (desc.charAt(0) == '$') {
            String[] split = desc.split("\\.");
            if (!split[0].equals("$block")) {
                throw new IllegalArgumentException("Block variable expected, got: " + desc);
            }
            switch (split[1]) {
                case "dispenser": {
                    return new BlockReference() {
                        @Override
                        public Block getTarget(RecipeContext context, Recipe recipe) {
                            return context.event.getBlock();
                        }

                        @Override
                        public void validate(Recipe recipe) {}
                    };
                }
                case "filter": {
                    int N = Integer.parseInt(split[2]);
                    return new BlockReference() {
                        @Override
                        public Block getTarget(RecipeContext context, Recipe recipe) {
                            return context.filterResults.get(N).block;
                        }

                        @Override
                        public void validate(Recipe recipe) {
                            if (recipe.filters.get(N).getExpectedBlocks() < 1) {
                                throw new IllegalArgumentException("Filter " + N + " does not reference a block.");
                            }
                        }
                    };
                }
                case "action": {
                    int N = Integer.parseInt(split[2]);
                    if (N >= actionNumber) {
                        throw new IllegalArgumentException("Cannot reference ActionResult " + N + " from action " + actionNumber);
                    }
                    return new BlockReference() {
                        @Override
                        public Block getTarget(RecipeContext context, Recipe recipe) {
                            return context.actionResults.get(N).block;
                        }

                        @Override
                        public void validate(Recipe recipe) {
                            if (recipe.actions.get(N).getExpectedBlocks() < 1) {
                                throw new IllegalArgumentException("Action " + N + " does not reference a block.");
                            }
                        }
                    };
                }
                default:
                    throw new IllegalArgumentException("Expected 'dispenser', 'action', or 'filter' in block reference, got " + split[1]);
            }
        } else {
            throw new IllegalArgumentException("BlockReference requires a variable, got " + desc);
        }
    }
}
