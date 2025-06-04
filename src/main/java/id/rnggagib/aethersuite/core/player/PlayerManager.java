package id.rnggagib.aethersuite.core.player;

import id.rnggagib.aethersuite.AetherSuite;
import id.rnggagib.aethersuite.api.database.dao.PlayerDataDao;
import id.rnggagib.aethersuite.api.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class PlayerManager {
    private final AetherSuite plugin;
    private final Map<UUID, PlayerSession> playerSessions = new ConcurrentHashMap<>();
    private final Map<UUID, PlayerData> playerDataCache = new ConcurrentHashMap<>();
    private final Map<String, UUID> usernameCache = new ConcurrentHashMap<>();
    private final PlayerDataDao playerDataDao;
    
    public PlayerManager(AetherSuite plugin) {
        this.plugin = plugin;
        this.playerDataDao = plugin.getDatabaseManager().getPlayerDataDao();
        
        Bukkit.getServer().getPluginManager().registerEvents(new PlayerListener(plugin, this), plugin);
    }
    
    public void handlePlayerJoin(Player player) {
        UUID playerUuid = player.getUniqueId();
        String username = player.getName();
        
        playerDataDao.playerExists(playerUuid).thenAccept(exists -> {
            if (exists) {
                playerDataDao.getPlayerData(playerUuid).thenAccept(optionalData -> {
                    if (optionalData.isPresent()) {
                        PlayerData playerData = optionalData.get();
                        
                        if (!playerData.getUsername().equals(username)) {
                            playerData.setUsername(username);
                        }
                        
                        playerData.setLastLogin(Instant.now());
                        
                        playerDataCache.put(playerUuid, playerData);
                        usernameCache.put(username.toLowerCase(), playerUuid);
                        
                        createPlayerSession(player, playerData);
                        
                        playerDataDao.savePlayerData(playerData);
                    }
                });
            } else {
                createNewPlayerData(player);
            }
        }).exceptionally(ex -> {
            plugin.getLogger().log(Level.SEVERE, "Error handling player join for " + username, ex);
            return null;
        });
    }
    
    private void createNewPlayerData(Player player) {
        UUID playerUuid = player.getUniqueId();
        String username = player.getName();
        double startingBalance = plugin.getConfigManager().getConfig("config")
                .getDouble("modules.economy.starting-balance", 0.0);
        
        playerDataDao.createPlayerData(playerUuid, username).thenRun(() -> {
            playerDataDao.getPlayerData(playerUuid).thenAccept(optionalData -> {
                if (optionalData.isPresent()) {
                    PlayerData playerData = optionalData.get();
                    playerData.setBalance(startingBalance);
                    
                    playerDataCache.put(playerUuid, playerData);
                    usernameCache.put(username.toLowerCase(), playerUuid);
                    
                    createPlayerSession(player, playerData);
                    
                    playerDataDao.savePlayerData(playerData);
                }
            });
        }).exceptionally(ex -> {
            plugin.getLogger().log(Level.SEVERE, "Error creating player data for " + username, ex);
            return null;
        });
    }
    
    private void createPlayerSession(Player player, PlayerData playerData) {
        String detectedLocale = player.locale().getLanguage();
        
        PlayerSession session = new PlayerSession(
                player.getUniqueId(),
                Instant.now(),
                player.getAddress().getAddress().getHostAddress(),
                detectedLocale
        );
        
        playerSessions.put(player.getUniqueId(), session);
        
        if (playerData.getLanguage() == null && plugin.getConfigManager().getConfig("config")
                .getBoolean("general.auto-detect-language", true)) {
            String supportedLocale = getSupportedLocaleFor(detectedLocale);
            playerData.setLanguage(supportedLocale);
            playerDataDao.savePlayerData(playerData);
        }
    }
    
    public void handlePlayerQuit(Player player) {
        UUID playerUuid = player.getUniqueId();
        
        if (playerDataCache.containsKey(playerUuid)) {
            PlayerData playerData = playerDataCache.get(playerUuid);
            playerData.setLastLocation(player.getLocation());
            
            playerDataDao.savePlayerData(playerData).exceptionally(ex -> {
                plugin.getLogger().log(Level.SEVERE, "Error saving player data on quit for " + player.getName(), ex);
                return null;
            });
        }
        
        playerSessions.remove(playerUuid);
    }
    
    public void saveAllPlayerData() {
        for (Map.Entry<UUID, PlayerData> entry : playerDataCache.entrySet()) {
            UUID uuid = entry.getKey();
            PlayerData data = entry.getValue();
            
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                data.setLastLocation(player.getLocation());
            }
            
            playerDataDao.savePlayerData(data).exceptionally(ex -> {
                plugin.getLogger().log(Level.SEVERE, "Error saving player data for " + uuid, ex);
                return null;
            });
        }
    }
    
    public Optional<PlayerData> getPlayerData(UUID uuid) {
        return Optional.ofNullable(playerDataCache.get(uuid));
    }
    
    public Optional<PlayerData> getPlayerData(String username) {
        UUID uuid = usernameCache.get(username.toLowerCase());
        if (uuid != null) {
            return getPlayerData(uuid);
        }
        return Optional.empty();
    }
    
    public CompletableFuture<Optional<PlayerData>> loadPlayerData(UUID uuid) {
        return playerDataDao.getPlayerData(uuid).thenApply(optionalData -> {
            optionalData.ifPresent(data -> {
                playerDataCache.put(uuid, data);
                usernameCache.put(data.getUsername().toLowerCase(), uuid);
            });
            return optionalData;
        });
    }
    
    public CompletableFuture<Optional<PlayerData>> loadPlayerData(String username) {
        return playerDataDao.getPlayerData(username).thenApply(optionalData -> {
            optionalData.ifPresent(data -> {
                playerDataCache.put(data.getUuid(), data);
                usernameCache.put(username.toLowerCase(), data.getUuid());
            });
            return optionalData;
        });
    }
    
    public void updatePlayerSetting(UUID uuid, String key, String value) {
        Optional<PlayerData> optionalData = getPlayerData(uuid);
        if (optionalData.isPresent()) {
            PlayerData playerData = optionalData.get();
            Map<String, String> settings = parseSettings(playerData.getSettings());
            settings.put(key, value);
            playerData.setSettings(serializeSettings(settings));
            
            playerDataDao.savePlayerData(playerData);
        }
    }
    
    public String getPlayerSetting(UUID uuid, String key, String defaultValue) {
        Optional<PlayerData> optionalData = getPlayerData(uuid);
        if (optionalData.isPresent()) {
            PlayerData playerData = optionalData.get();
            Map<String, String> settings = parseSettings(playerData.getSettings());
            return settings.getOrDefault(key, defaultValue);
        }
        return defaultValue;
    }
    
    public boolean hasPlayerSession(UUID uuid) {
        return playerSessions.containsKey(uuid);
    }
    
    public Optional<PlayerSession> getPlayerSession(UUID uuid) {
        return Optional.ofNullable(playerSessions.get(uuid));
    }
    
    public void setPlayerLanguage(UUID uuid, String language) {
        Optional<PlayerData> optionalData = getPlayerData(uuid);
        if (optionalData.isPresent()) {
            PlayerData playerData = optionalData.get();
            playerData.setLanguage(language);
            
            playerDataDao.savePlayerData(playerData);
            
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                plugin.getMessageProvider().sendPrefixedMessage(player, "general.language-changed", 
                        Map.of("language", getLanguageDisplayName(language)));
            }
        }
    }
    
    public String getPlayerLanguage(UUID uuid) {
        Optional<PlayerData> optionalData = getPlayerData(uuid);
        if (optionalData.isPresent()) {
            PlayerData playerData = optionalData.get();
            if (playerData.getLanguage() != null) {
                return playerData.getLanguage();
            }
        }
        
        return plugin.getConfigManager().getConfig("config").getString("general.language", "en_US");
    }
    
    private Map<String, String> parseSettings(String settingsStr) {
        Map<String, String> settings = new HashMap<>();
        
        if (settingsStr == null || settingsStr.isEmpty()) {
            return settings;
        }
        
        String[] pairs = settingsStr.split(";");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=", 2);
            if (keyValue.length == 2) {
                settings.put(keyValue[0], keyValue[1]);
            }
        }
        
        return settings;
    }
    
    private String serializeSettings(Map<String, String> settings) {
        if (settings.isEmpty()) {
            return "";
        }
        
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> entry : settings.entrySet()) {
            if (builder.length() > 0) {
                builder.append(";");
            }
            builder.append(entry.getKey()).append("=").append(entry.getValue());
        }
        
        return builder.toString();
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
    
    private String getLanguageDisplayName(String localeCode) {
        Map<String, String> languageNames = new HashMap<>();
        languageNames.put("en_US", "English (US)");
        languageNames.put("id_ID", "Bahasa Indonesia");
        languageNames.put("ja_JP", "日本語");
        languageNames.put("zh_CN", "简体中文");
        languageNames.put("zh_TW", "繁體中文");
        languageNames.put("de_DE", "Deutsch");
        languageNames.put("es_ES", "Español");
        languageNames.put("fr_FR", "Français");
        languageNames.put("it_IT", "Italiano");
        languageNames.put("ko_KR", "한국어");
        languageNames.put("pt_BR", "Português (Brasil)");
        languageNames.put("pt_PT", "Português (Portugal)");
        languageNames.put("ru_RU", "Русский");
        
        return languageNames.getOrDefault(localeCode, localeCode);
    }
    
    public void updateLastLocation(UUID uuid, Location location) {
        Optional<PlayerData> optionalData = getPlayerData(uuid);
        if (optionalData.isPresent()) {
            PlayerData playerData = optionalData.get();
            playerData.setLastLocation(location);
        }
    }
    
    public void updatePlayerBalance(UUID uuid, double newBalance) {
        Optional<PlayerData> optionalData = getPlayerData(uuid);
        if (optionalData.isPresent()) {
            PlayerData playerData = optionalData.get();
            playerData.setBalance(newBalance);
            playerDataDao.updateBalance(uuid, newBalance);
        }
    }
    
    public double getPlayerBalance(UUID uuid) {
        Optional<PlayerData> optionalData = getPlayerData(uuid);
        return optionalData.map(PlayerData::getBalance).orElse(0.0);
    }
    
    public void toggleTeleportAccept(UUID uuid, boolean acceptTeleport) {
        Optional<PlayerData> optionalData = getPlayerData(uuid);
        if (optionalData.isPresent()) {
            PlayerData playerData = optionalData.get();
            playerData.setAcceptTeleport(acceptTeleport);
            playerDataDao.savePlayerData(playerData);
        }
    }
    
    public boolean doesAcceptTeleport(UUID uuid) {
        Optional<PlayerData> optionalData = getPlayerData(uuid);
        return optionalData.map(PlayerData::isAcceptTeleport).orElse(true);
    }
}