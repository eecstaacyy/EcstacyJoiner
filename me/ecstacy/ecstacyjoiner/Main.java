package me.ecstacy.ecstacyjoiner;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    private PlayerListener playerListener;

    @Override
    public void onEnable() {
        this.playerListener = new PlayerListener(this);
        PlayerCommandHandler commandHandler = new PlayerCommandHandler(this);
        getCommand("ejoiner").setExecutor(commandHandler);
        getLogger().info("EcstacyJoiner has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("EcstacyJoiner has been disabled.");
    }

    public PlayerListener getPlayerListener() {
        return playerListener;
    }

    public static String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
