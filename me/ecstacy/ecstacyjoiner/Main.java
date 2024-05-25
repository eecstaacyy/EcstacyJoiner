package me.ecstacy.ecstacyjoiner;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    @Override
    public void onEnable() {
        new PlayerListener(this);
        getLogger().info("EcstacyJoiner has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("EcstacyJoiner has been disabled.");
    }

    public static String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
