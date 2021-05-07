package com.caucraft.customdispensers.command;

import com.caucraft.customdispensers.CustomDispensers;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

public class CmdCustomdispensers implements CommandExecutor {

    private final CustomDispensers plugin;

    public CmdCustomdispensers(CustomDispensers plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            return false;
        } else switch (args[0]) {
            case "reload": {
                try {
                    plugin.reloadDispenserConfig();
                    sender.sendMessage("Configuration reloaded!");
                    plugin.getLogger().log(Level.INFO, sender.getName() + " reloaded the configuration.");
                } catch (Exception ex) {
                    sender.sendMessage("Exception while reloading the configuration: " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
                    plugin.getLogger().log(Level.WARNING, "Could not reload configuration.", ex);
                }
                break;
            }
            default: {
                return false;
            }
        }

        return true;
    }
}
