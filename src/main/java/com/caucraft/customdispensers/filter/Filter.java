package com.caucraft.customdispensers.filter;

import com.caucraft.customdispensers.ActionResult;
import com.caucraft.customdispensers.Config;
import org.bukkit.block.Block;

import java.util.Map;

public interface Filter {
    ActionResult apply(Block dspr);

    default int getExpectedItems() { return 0; }
    default int getExpectedBlocks() { return 0; }
    default int getExpectedEntities() { return 0; }

    static Filter parseFilter(Config config, Map<?, ?> filterData) {
        switch (((String) filterData.get("filter")).toLowerCase()) {
            case "target_block": return BlockFilter.parseFilter(config, filterData);
            case "target_entity": return EntityFilter.parseFilter(config, filterData);
            default: throw new IllegalArgumentException(filterData.get("filter") + " is not a validi filter type");
        }
    }
}
