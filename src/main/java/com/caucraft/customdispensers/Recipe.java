package com.caucraft.customdispensers;

import com.caucraft.customdispensers.action.Action;
import com.caucraft.customdispensers.filter.DispenserFilter;
import com.caucraft.customdispensers.filter.Filter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;

public class Recipe {

    public final DispenserFilter dsprFilter;
    public final ItemStack dropItem;
    public final boolean cancelEvent;
    public final boolean softFail;
    public final Permission permission;
    public final int permissionFilter;
    public final List<Filter> filters;
    public final List<Action> actions;

    public Recipe(DispenserFilter dsprFilter, ItemStack dropItem, boolean cancelEvent, boolean softFail,
              String permission, int permissionFilter, List<Filter> filters, List<Action> actions) {
        this.dsprFilter = dsprFilter;
        this.dropItem = dropItem;
        this.cancelEvent = cancelEvent;
        this.softFail = softFail;
        this.permission = permission == null ? null : new Permission("customdispensers.recipe." + permission, PermissionDefault.FALSE);
        this.permissionFilter = permissionFilter;
        this.filters = filters;
        this.actions = actions;
    }

    public void validate() {
//        for (int i = 0; i < filters.size(); i++) {
//        }
        for (int i = 0; i < actions.size(); i++) {
            actions.get(i).validate(this, i);
        }
        if (permission != null) {
            if (permissionFilter < 0 || permissionFilter >= filters.size()) {
                throw new IllegalArgumentException("Permission filter references a filter that doesn't exist: " + permissionFilter);
            }
            if (filters.get(permissionFilter).getExpectedEntities() < 1) {
                throw new IllegalArgumentException("Permission filter references a filter that does not target any entities.");
            }
        }
        // TODO throw exception if variable arguments reference invalid... things
    }

    public static Recipe createRecipe(CustomDispensers plugin, Config config, Map<String, ItemStack> itemMap, Map<Object, Object> recipeData) {
        Map<?, ?> tempMap = (Map<?, ?>) recipeData.get("dispenser");

        // Parse dispenser filter (required)
        DispenserFilter dsprFilter = new DispenserFilter(
                Config.getEnum((String) tempMap.get("type"), Material.class, "Check the Material javadoc for your server version."),
                (String) tempMap.get("name"),
                tempMap.containsKey("direction") ? Config.getEnum((String) tempMap.get("direction"), Direction.class, null) : null);

        // Parse item (required)
        ItemStack dropItem = Objects.requireNonNull(itemMap.get((String) recipeData.get("drop-item")), () -> recipeData.get("drop-item") + " is not a defined item name");

        // Parse cancel option
        boolean cancelEvent = (Boolean) recipeData.getOrDefault("cancel-event", true);
        boolean softFail = (Boolean) recipeData.getOrDefault("soft-fail", false);

        String permission = (String) recipeData.getOrDefault("permission", null);
        int permissionFilter = permission == null ? -1 : (Integer) recipeData.get("permission-filter");

        String recipeReference = dsprFilter.type.name()
                + (dsprFilter.name == null ? "" : ":" + dsprFilter.name)
                + "->" + dropItem.getType();

        // Parse filters
        List<Filter> filters = new ArrayList<>();
        List<?> tempList = (List<?>) recipeData.get("filters");
        if (tempList != null) {
            for (int i = 0; i < tempList.size(); i++) {
                Object fobj = tempList.get(i);
                try {
                    Map<?, ?> filterData = (Map<?, ?>) fobj;
                    filters.add(Filter.parseFilter(config, filterData));
                } catch (Exception ex) {
                    plugin.getLogger().log(Level.WARNING, "Filter " + i + " in " + recipeReference + " could not be parsed", ex);
                }
            }
        }

        // Parse actions
        List<Action> actions = new ArrayList<>();
        tempList = (List<?>) recipeData.get("actions");
        if (tempList != null) {
            for (int i = 0; i < tempList.size(); i++) {
                Object aobj = tempList.get(i);
                try {
                    String action = (String) aobj;
                    actions.add(Action.parseAction(config, action, i));
                } catch (Exception ex) {
                    plugin.getLogger().log(Level.WARNING, "Action " + i + " in " + recipeReference + " could not be parsed", ex);
                    throw ex;
                }
            }
        }

        return new Recipe(dsprFilter, dropItem, cancelEvent, softFail, permission, permissionFilter, filters, actions);
    }
}
