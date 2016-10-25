package com.darkblade12.itemslotmachine.hook;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

@SuppressWarnings("unchecked")
public abstract class Hook<P extends JavaPlugin> {
    protected static boolean ENABLED;
    protected P plugin;

    public static boolean isEnabled() {
        return ENABLED;
    }

    public boolean load() {
        Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin(getPluginName());
        if (plugin != null) {
            this.plugin = (P) plugin;
            ENABLED = initialize();
        }
        return plugin != null && ENABLED;
    }

    protected abstract boolean initialize();

    public abstract String getPluginName();

    public P getPlugin() {
        return this.plugin;
    }
}