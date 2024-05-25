package me.ecstacy.ecstacyjoiner;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PlayerListener implements Listener, CommandExecutor {
    private Main plugin;
    private Map<String, String> playerJoiners = new HashMap<>();
    private Permission perms = null;
    private Chat chat = null;
    private File playerDataFile;
    private FileConfiguration playerData;

    public PlayerListener(Main plugin) {
        this.plugin = plugin;
        this.playerDataFile = new File(plugin.getDataFolder(), "playerData.yml");
        if (!this.playerDataFile.exists()) {
            try {
                this.playerDataFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.playerData = YamlConfiguration.loadConfiguration(this.playerDataFile);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        plugin.getCommand("ejoiner").setExecutor(this);
        plugin.getCommand("ecstacyjoiner").setExecutor(this);
        setupPermissions();
        setupChat();
        plugin.saveDefaultConfig();
    }

    private void setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = plugin.getServer().getServicesManager().getRegistration(Permission.class);
        if (rsp != null) {
            perms = rsp.getProvider();
        }
    }

    private void setupChat() {
        RegisteredServiceProvider<Chat> rsp = plugin.getServer().getServicesManager().getRegistration(Chat.class);
        if (rsp != null) {
            chat = rsp.getProvider();
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Main.colorize("&cThis command can only be used by players."));
            return true;
        }
        Player player = (Player) sender;
        FileConfiguration config = plugin.getConfig();

        if (args.length == 0 || (args.length == 1 && args[0].equalsIgnoreCase("set"))) {
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
                playerJoiners.put(player.getName(), joinerId);
                playerData.set(player.getUniqueId().toString(), joinerId);
                savePlayerData();
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

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        FileConfiguration config = plugin.getConfig();

        String joinerId = playerData.getString(player.getUniqueId().toString(), null);
        if (joinerId == null || !config.contains("joiners." + joinerId)) {
            joinerId = null;
            for (String key : config.getConfigurationSection("joiners").getKeys(false)) {
                String permission = config.getString("joiners." + key + ".permission");
                if (permission == null || (perms != null && perms.has(player, permission))) {
                    joinerId = key;
                }
            }
            if (joinerId != null) {
                playerJoiners.put(player.getName(), joinerId);
                playerData.set(player.getUniqueId().toString(), joinerId);
                savePlayerData();
            } else {
                playerJoiners.remove(player.getName());
            }
        } else {
            playerJoiners.put(player.getName(), joinerId);
        }

        String message = joinerId != null ? config.getString("joiners." + joinerId + ".message") : null;

        if (message != null) {
            if (chat != null) {
                String prefix = Main.colorize(chat.getPlayerPrefix(player));
                message = message.replace("{prefix}", prefix).replace("{nickname}", player.getName());
            } else {
                message = message.replace("{prefix}", "").replace("{nickname}", player.getName());
            }
            message = Main.colorize(message);
            event.setJoinMessage(message);
        } else {
            event.setJoinMessage(null);
        }
    }

    private void savePlayerData() {
        try {
            playerData.save(playerDataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
