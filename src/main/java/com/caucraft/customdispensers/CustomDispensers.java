package com.caucraft.customdispensers;

import com.caucraft.customdispensers.command.CmdCustomdispensers;
import com.caucraft.customdispensers.command.CmdItemyaml;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.permissions.DefaultPermissions;

public final class CustomDispensers extends JavaPlugin {

    private Config config;
    private EventListener listener;

    private CmdCustomdispensers cmdCd;
    private CmdItemyaml cmdItemyaml;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        config = new Config(this, getConfig());
        listener = new EventListener(this);

        cmdCd = new CmdCustomdispensers(this);
        cmdItemyaml = new CmdItemyaml(this);
        getCommand("customdispensers").setExecutor(cmdCd);
        getCommand("itemyaml").setExecutor(cmdItemyaml);


        getServer().getPluginManager().registerEvents(listener, this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public void reloadDispenserConfig() {
        reloadConfig();
        Config newConfig = new Config(this, getConfig());
        this.config = newConfig;
    }

    public Config getDispenserConfig() {
        return config;
    }
}
