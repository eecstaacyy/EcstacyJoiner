package me.ecstacy.ecstacyjoiner;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;
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

public class PlayerListener implements Listener {
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
                plugin.getLogger().info("playerData.yml file created.");
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create playerData.yml file: " + e.getMessage());
            }
        }
        this.playerData = YamlConfiguration.loadConfiguration(this.playerDataFile);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
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

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        FileConfiguration config = plugin.getConfig();

        String joinerId = playerData.getString(player.getUniqueId().toString(), null);
        
        if (joinerId != null && (!config.contains("joiners." + joinerId) || !perms.has(player, config.getString("joiners." + joinerId + ".permission")))) {
            joinerId = null;
        }

        if (joinerId == null) {
            for (String key : config.getConfigurationSection("joiners").getKeys(false)) {
                String permission = config.getString("joiners." + key + ".permission");
                if (permission == null || perms.has(player, permission)) {
                    joinerId = key;
                    break;
                }
            }
        }

        if (joinerId != null) {
            playerJoiners.put(player.getName(), joinerId);
            playerData.set(player.getUniqueId().toString(), joinerId);
            savePlayerData();
        } else {
            playerJoiners.remove(player.getName());
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

    public FileConfiguration getPlayerData() {
        return playerData;
    }

    public void savePlayerData() {
        try {
            playerData.save(playerDataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save playerData.yml file: " + e.getMessage());
        }
    }
}
