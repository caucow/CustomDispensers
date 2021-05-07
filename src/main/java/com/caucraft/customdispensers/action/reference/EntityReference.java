package com.caucraft.customdispensers.action.reference;

import com.caucraft.customdispensers.Config;
import com.caucraft.customdispensers.Recipe;
import com.caucraft.customdispensers.RecipeContext;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

public interface EntityReference extends Reference<Entity> {
    static EntityReference getReference(Config config, String desc, int actionNumber) {
        if (desc.charAt(0) == '$') {
            String[] split = desc.split("\\.");
            if (!split[0].equals("$entity")) {
                throw new IllegalArgumentException("Entity variable expected, got: " + desc);
            }
            switch (split[1]) {
                case "filter": {
                    int N = Integer.parseInt(split[2]);
                    return new EntityReference() {
                        @Override
                        public Entity getTarget(RecipeContext context, Recipe recipe) {
                            return context.filterResults.get(N).entity;
                        }

                        @Override
                        public void validate(Recipe recipe) {
                            if (recipe.filters.get(N).getExpectedEntities() < 1) {
                                throw new IllegalArgumentException("Filter " + N + " does not reference an entity.");
                            }
                        }
                    };
                }
                case "action": {
                    int N = Integer.parseInt(split[2]);
                    if (N >= actionNumber) {
                        throw new IllegalArgumentException("Cannot reference ActionResult " + N + " from action " + actionNumber);
                    }
                    return new EntityReference() {
                        @Override
                        public Entity getTarget(RecipeContext context, Recipe recipe) {
                            return context.actionResults.get(N).entity;
                        }

                        @Override
                        public void validate(Recipe recipe) {
                            if (recipe.actions.get(N).getExpectedEntities() < 1) {
                                throw new IllegalArgumentException("Action " + N + " does not reference an entity.");
                            }
                        }
                    };
                }
                default:
                    throw new IllegalArgumentException("Expected 'action' or 'filter' in entity reference, got " + split[1]);
            }
        } else {
            throw new IllegalArgumentException("EntityReference requires a variable, got " + desc);
        }
    }
}
