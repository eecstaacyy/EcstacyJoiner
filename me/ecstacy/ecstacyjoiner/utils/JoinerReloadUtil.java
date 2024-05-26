package me.ecstacy.ecstacyjoiner.utils;

import me.ecstacy.ecstacyjoiner.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class JoinerReloadUtil implements CommandExecutor {
    private Main plugin;

    public JoinerReloadUtil(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.hasPermission("ejoiner.reload")) {
            plugin.reloadConfig();
            String reloadMessage = plugin.getConfig().getString("joiner_successfully_reloaded");
            sender.sendMessage(Main.colorize(reloadMessage));
        } else {
            String noPermissionMessage = plugin.getConfig().getString("no_permission");
            sender.sendMessage(Main.colorize(noPermissionMessage));
        }
        return true;
    }
}

