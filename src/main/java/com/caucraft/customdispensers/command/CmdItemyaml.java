package com.caucraft.customdispensers.command;

import com.caucraft.customdispensers.CustomDispensers;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

public class CmdItemyaml implements CommandExecutor {

    private final CustomDispensers plugin;

    public CmdItemyaml(CustomDispensers plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Command must be run by a player.");
            return true;
        }
        ItemStack held = ((Player) sender).getInventory().getItemInMainHand();
        if (held == null) {
            sender.sendMessage("Must be holding an item in your main hand.");
            return true;
        }
        YamlConfiguration conf = new YamlConfiguration();
        conf.set("the-item", held);
        String itemyaml = conf.saveToString();
        sender.sendMessage(itemyaml);
        plugin.getLogger().log(Level.INFO, sender.getName() + " got item info");
        return true;
    }
}
