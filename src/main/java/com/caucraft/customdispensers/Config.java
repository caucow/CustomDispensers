package com.caucraft.customdispensers;

import com.caucraft.customdispensers.action.Action;
import com.caucraft.customdispensers.filter.EntityFilter;
import com.caucraft.customdispensers.filter.Filter;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class Config {

    private static final ListMultimap<Material, Recipe> EMPTY_MAP = ArrayListMultimap.create();

    private final CustomDispensers plugin;
    private final Map<String, ItemStack> itemMap;
    private final Map<Material, ListMultimap<Material, Recipe>> recipeMap;

    public Config(CustomDispensers plugin, FileConfiguration config) {
        this.plugin = plugin;
        this.itemMap = new HashMap<>();
        ConfigurationSection subsec = config.getConfigurationSection("items");
        if (subsec != null) {
            for (String itemName : subsec.getKeys(false)) {
                ItemStack stack = subsec.getItemStack(itemName);
                if (stack == null) {
                    plugin.getLogger().log(Level.WARNING, "Malformed ItemStack under items." + itemName);
                } else {
                    itemMap.put(itemName, stack);
                }
            }
        }
        this.recipeMap = new HashMap<>();
        if (config.contains("recipes")) {
            for (Map<?, ?> recipeData : config.getMapList("recipes")) {
                try {
                    // honestly fuck <?> generics
                    @SuppressWarnings("unchecked")
                    Recipe recipe = Recipe.createRecipe(plugin, this, itemMap, (Map<Object, Object>) recipeData);
                    recipe.validate();
                    addRecipe(recipe.dsprFilter.type, recipe.dropItem.getType(), recipe);
                } catch (Exception ex) {
                    plugin.getLogger().log(Level.WARNING, "Could not load recipe", ex);
                }
            }
        }
    }

    private void addRecipe(Material dispenserType, Material itemType, Recipe recipe) {
        recipeMap
                .computeIfAbsent(dispenserType, (dt) -> ArrayListMultimap.create())
                .put(itemType, recipe);
        String recipeReference = recipe.dsprFilter.type.name()
                + (recipe.dsprFilter.name == null ? "" : ":" + recipe.dsprFilter.name)
                + "->" + recipe.dropItem.getType()
                + "=>" + recipe.filters.size() + " filters;"
                + recipe.actions.size() + " actions";
        plugin.getLogger().log(Level.INFO, "Added recipe: " + recipeReference);
    }

    public List<Recipe> getRecipes(Material dispenserType, Material itemType) {
        return recipeMap.getOrDefault(dispenserType, EMPTY_MAP).get(itemType);
    }

    public ItemStack getItem(String name) {
        return itemMap.get(name);
    }

    /*
     * Case insensitive enum thingy. Throws exception listing values if incorrect value given.
     */
    public static <T extends Enum<T>> T getEnum(String name, Class<T> clazz, String optErrorMsg) {
        T[] ts = clazz.getEnumConstants();
        for (T t : ts) {
            if (t.name().equalsIgnoreCase(name)) {
                return t;
            }
        }
        if (optErrorMsg == null) {
            throw new IllegalArgumentException("Enum " + clazz.getSimpleName() + " only has values: " + Arrays.toString(ts));
        } else {
            throw new IllegalArgumentException(name + " is not a valid name in " + clazz.getSimpleName() + ": " + optErrorMsg);
        }
    }

}
