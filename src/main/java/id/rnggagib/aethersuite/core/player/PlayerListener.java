package id.rnggagib.aethersuite.core.player;

import id.rnggagib.aethersuite.AetherSuite;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLocaleChangeEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import id.rnggagib.aethersuite.api.player.PlayerData;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class PlayerListener implements Listener {
    private final AetherSuite plugin;
    private final PlayerManager playerManager;
    
    public PlayerListener(AetherSuite plugin, PlayerManager playerManager) {
        this.plugin = plugin;
        this.playerManager = playerManager;
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        UUID uuid = event.getUniqueId();
        String username = event.getName();
        
        // Preload player data to minimize login delay
        playerManager.loadPlayerData(uuid).exceptionally(ex -> {
            plugin.getLogger().severe("Failed to preload player data for " + username + ": " + ex.getMessage());
            return Optional.empty();
        });
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        playerManager.handlePlayerJoin(player);
        
        // Apply player's language preference
        String language = playerManager.getPlayerLanguage(player.getUniqueId());
        Optional<PlayerSession> session = playerManager.getPlayerSession(player.getUniqueId());
        
        if (session.isPresent()) {
            session.get().updateActivity();
            session.get().setLastLocation(player.getLocation());
        }
        
        // Send welcome message if it's the first time
        if (!playerManager.getPlayerData(player.getUniqueId()).isPresent()) {
            plugin.getScheduler().runTaskLater(() -> {
                plugin.getMessageProvider().sendPrefixedMessage(player, "general.welcome",
                        Map.of("player", player.getName()));
            }, 10L);
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        playerManager.handlePlayerQuit(player);
    }
    
    @EventHandler
    public void onPlayerLocaleChange(PlayerLocaleChangeEvent event) {
        Player player = event.getPlayer();
        String newLocale = event.getLocale();
        
        if (plugin.getConfigManager().getConfig("config").getBoolean("general.auto-detect-language", true)) {
            Optional<PlayerData> playerData = playerManager.getPlayerData(player.getUniqueId());
            if (playerData.isPresent()) {
                // Update language based on client locale change
                if (playerData.get().getLanguage() == null) {
                    String supportedLocale = getSupportedLocaleFor(newLocale);
                    playerManager.setPlayerLanguage(player.getUniqueId(), supportedLocale);
                }
            }
        }
    }
    
    private String getSupportedLocaleFor(String clientLocale) {
        if (clientLocale == null) {
            return plugin.getConfigManager().getConfig("config").getString("general.language", "en_US");
        }
        
        String lowerLocale = clientLocale.toLowerCase();
        
        if (lowerLocale.startsWith("en")) {
            return "en_US";
        } else if (lowerLocale.startsWith("id")) {
            return "id_ID";
        } else if (lowerLocale.startsWith("ja")) {
            return "ja_JP";
        } else if (lowerLocale.startsWith("zh")) {
            if (lowerLocale.contains("tw") || lowerLocale.contains("hk")) {
                return "zh_TW"; // Traditional Chinese
            }
            return "zh_CN"; // Simplified Chinese
        } else if (lowerLocale.startsWith("de")) {
            return "de_DE";
        } else if (lowerLocale.startsWith("es")) {
            return "es_ES";
        } else if (lowerLocale.startsWith("fr")) {
            return "fr_FR";
        } else if (lowerLocale.startsWith("it")) {
            return "it_IT";
        } else if (lowerLocale.startsWith("ko")) {
            return "ko_KR";
        } else if (lowerLocale.startsWith("pt")) {
            if (lowerLocale.contains("br")) {
                return "pt_BR";
            }
            return "pt_PT";
        } else if (lowerLocale.startsWith("ru")) {
            return "ru_RU";
        }
        
        // Default fallback
        return plugin.getConfigManager().getConfig("config").getString("general.language", "en_US");
    }
}