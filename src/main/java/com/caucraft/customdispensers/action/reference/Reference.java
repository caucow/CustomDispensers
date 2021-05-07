package com.caucraft.customdispensers.action.reference;

import com.caucraft.customdispensers.Recipe;
import com.caucraft.customdispensers.RecipeContext;

public interface Reference<T> {
    T getTarget(RecipeContext context, Recipe recipe);
    void validate(Recipe recipe);
}
