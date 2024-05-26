package me.ecstacy.ecstacyjoiner;

import net.milkbowl.vault.permission.Permission;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class PlayerCommandHandler implements CommandExecutor {
    private Main plugin;
    private Permission perms;

    public PlayerCommandHandler(Main plugin) {
        this.plugin = plugin;
        setupPermissions();
    }

    private void setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = plugin.getServer().getServicesManager().getRegistration(Permission.class);
        if (rsp != null) {
            perms = rsp.getProvider();
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        FileConfiguration config = plugin.getConfig();

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (sender instanceof ConsoleCommandSender || sender.hasPermission("ejoiner.reload")) {
                plugin.reloadConfig();
                String reloadMessage = config.getString("joiner_successfully_reloaded");
                if (reloadMessage != null) {
                    sender.sendMessage(Main.colorize(reloadMessage));
                }
            } else {
                String noPermissionMessage = config.getString("no_permission");
                if (noPermissionMessage != null) {
                    sender.sendMessage(Main.colorize(noPermissionMessage));
                }
            }
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(Main.colorize("&cThis command can only be used by players."));
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0 || (args.length == 1 && args[0].equalsIgnoreCase("set"))) {
            boolean hasAnyPermission = false;
            for (String key : config.getConfigurationSection("joiners").getKeys(false)) {
                String permission = config.getString("joiners." + key + ".permission");
                if (permission != null && perms.has(player, permission)) {
                    hasAnyPermission = true;
                    break;
                }
            }
            if (!hasAnyPermission) {
                String noPermissionMessage = config.getString("no_permission");
                if (noPermissionMessage != null) {
                    player.sendMessage(Main.colorize(noPermissionMessage));
                }
                return true;
            }

            String helpMessage = config.getString("joiner_help_message");
            if (helpMessage != null) {
                player.sendMessage(Main.colorize(helpMessage));
            }
            return true;
        } else if (args.length == 2 && args[0].equalsIgnoreCase("set")) {
            String joinerId = args[1];
            if (config.contains("joiners." + joinerId)) {
                String permission = config.getString("joiners." + joinerId + ".permission");
                if (permission != null && perms != null && !perms.has(player, permission)) {
                    String noPermissionMessage = config.getString("no_permission");
                    if (noPermissionMessage != null) {
                        player.sendMessage(Main.colorize(noPermissionMessage));
                    }
                    return true;
                }

                String currentJoinerId = plugin.getPlayerListener().getPlayerData().getString(player.getUniqueId().toString(), null);
                if (joinerId.equals(currentJoinerId)) {
                    String joinerAlreadySetMessage = config.getString("joiner_already_set");
                    if (joinerAlreadySetMessage != null) {
                        player.sendMessage(Main.colorize(joinerAlreadySetMessage));
                    }
                    return true;
                }

                plugin.getPlayerListener().getPlayerData().set(player.getUniqueId().toString(), joinerId);
                plugin.getPlayerListener().savePlayerData();
                String joinerSuccessfullySetMessage = config.getString("joiner_successfully_set");
                if (joinerSuccessfullySetMessage != null) {
                    player.sendMessage(Main.colorize(joinerSuccessfullySetMessage));
                }
            } else {
                String joinerNotFoundMessage = config.getString("joiner_not_found");
                if (joinerNotFoundMessage != null) {
                    player.sendMessage(Main.colorize(joinerNotFoundMessage));
                }
            }
            return true;
        }
        return false;
    }
}
