package id.rnggagib.aethersuite.core.home;

import id.rnggagib.aethersuite.AetherSuite;
import id.rnggagib.aethersuite.api.database.dao.HomeDao;
import id.rnggagib.aethersuite.api.home.Home;
import id.rnggagib.aethersuite.core.teleport.TeleportManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.regex.Pattern;

public class HomeManager {
    private final AetherSuite plugin;
    private final HomeDao homeDao;
    private final TeleportManager teleportManager;
    private final Pattern validNamePattern = Pattern.compile("^[a-zA-Z0-9_-]{1,16}$");
    
    public HomeManager(AetherSuite plugin) {
        this.plugin = plugin;
        this.homeDao = plugin.getDatabaseManager().getHomeDao();
        this.teleportManager = plugin.getTeleportManager();
    }
    
    public CompletableFuture<Boolean> setHome(Player player, String name) {
        UUID playerUuid = player.getUniqueId();
        Location location = player.getLocation();
        
        if (!isValidHomeName(name)) {
            plugin.getMessageProvider().sendPrefixedMessage(player, "modules.player.homes.invalid-name");
            return CompletableFuture.completedFuture(false);
        }
        
        return homeDao.homeExists(playerUuid, name).thenCompose(exists -> {
            if (exists) {
                return homeDao.updateHome(playerUuid, name, location).thenApply(success -> {
                    if (success) {
                        plugin.getMessageProvider().sendPrefixedMessage(player, "modules.player.homes.updated", 
                                Map.of("name", name));
                    } else {
                        plugin.getMessageProvider().sendPrefixedMessage(player, "general.error");
                    }
                    return success;
                });
            } else {
                return homeDao.countHomes(playerUuid).thenCompose(count -> {
                    int limit = getHomeLimit(player);
                    
                    if (count >= limit) {
                        plugin.getMessageProvider().sendPrefixedMessage(player, "modules.player.homes.limit-reached", 
                                Map.of("limit", String.valueOf(limit)));
                        return CompletableFuture.completedFuture(false);
                    }
                    
                    return homeDao.createHome(playerUuid, name, location).thenApply(homeId -> {
                        if (homeId > 0) {
                            plugin.getMessageProvider().sendPrefixedMessage(player, "modules.player.homes.set", 
                                    Map.of("name", name));
                            return true;
                        } else {
                            plugin.getMessageProvider().sendPrefixedMessage(player, "general.error");
                            return false;
                        }
                    });
                });
            }
        }).exceptionally(ex -> {
            plugin.getLogger().log(Level.SEVERE, "Error setting home for " + player.getName(), ex);
            plugin.getMessageProvider().sendPrefixedMessage(player, "general.error");
            return false;
        });
    }
    
    public CompletableFuture<Boolean> deleteHome(Player player, String name) {
        UUID playerUuid = player.getUniqueId();
        
        return homeDao.deleteHome(playerUuid, name).thenApply(success -> {
            if (success) {
                plugin.getMessageProvider().sendPrefixedMessage(player, "modules.player.homes.deleted", 
                        Map.of("name", name));
            } else {
                plugin.getMessageProvider().sendPrefixedMessage(player, "modules.player.homes.not-found", 
                        Map.of("name", name));
            }
            return success;
        }).exceptionally(ex -> {
            plugin.getLogger().log(Level.SEVERE, "Error deleting home for " + player.getName(), ex);
            plugin.getMessageProvider().sendPrefixedMessage(player, "general.error");
            return false;
        });
    }
    
    public CompletableFuture<List<Home>> getHomes(Player player) {
        UUID playerUuid = player.getUniqueId();
        
        return homeDao.getHomes(playerUuid).exceptionally(ex -> {
            plugin.getLogger().log(Level.SEVERE, "Error getting homes for " + player.getName(), ex);
            return List.of();
        });
    }
    
    public CompletableFuture<Boolean> teleportToHome(Player player, String name) {
        UUID playerUuid = player.getUniqueId();
        
        return homeDao.getHome(playerUuid, name).thenApply(optionalHome -> {
            if (optionalHome.isEmpty()) {
                plugin.getMessageProvider().sendPrefixedMessage(player, "modules.player.homes.not-found", 
                        Map.of("name", name));
                return false;
            }
            
            Home home = optionalHome.get();
            Location location = home.getLocation();
            
            // Use the teleport manager to handle warmup, cooldown, etc.
            teleportManager.teleportPlayerWithWarmup(player, location, PlayerTeleportEvent.TeleportCause.COMMAND);
            
            return true;
        }).exceptionally(ex -> {
            plugin.getLogger().log(Level.SEVERE, "Error teleporting " + player.getName() + " to home", ex);
            plugin.getMessageProvider().sendPrefixedMessage(player, "general.error");
            return false;
        });
    }
    
    public int getHomeLimit(Player player) {
        int defaultLimit = plugin.getConfigManager().getConfig("config")
                .getInt("modules.player.homes.max-default", 3);
        
        Map<String, Object> permissionLimits = plugin.getConfigManager().getConfig("config")
                .getConfigurationSection("modules.player.homes.max-per-permission")
                .getValues(false);
        
        int highestLimit = defaultLimit;
        
        for (Map.Entry<String, Object> entry : permissionLimits.entrySet()) {
            String permission = entry.getKey();
            if (player.hasPermission(permission)) {
                int permLimit = (int) entry.getValue();
                if (permLimit > highestLimit) {
                    highestLimit = permLimit;
                }
            }
        }
        
        return highestLimit;
    }
    
    public boolean isValidHomeName(String name) {
        return name != null && validNamePattern.matcher(name).matches();
    }
    
    public CompletableFuture<Optional<Home>> getHome(UUID playerUuid, String name) {
        return homeDao.getHome(playerUuid, name);
    }
}