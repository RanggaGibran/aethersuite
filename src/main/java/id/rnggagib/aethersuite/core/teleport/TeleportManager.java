package id.rnggagib.aethersuite.core.teleport;

import id.rnggagib.aethersuite.AetherSuite;
import id.rnggagib.aethersuite.core.player.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class TeleportManager {
    private final AetherSuite plugin;
    private final Map<UUID, Map<UUID, TeleportRequest>> teleportRequests = new ConcurrentHashMap<>();
    private final Map<UUID, Instant> lastTeleport = new ConcurrentHashMap<>();
    private final Map<UUID, TeleportWarmup> warmupTasks = new ConcurrentHashMap<>();
    private final Map<UUID, Location> lastLocations = new ConcurrentHashMap<>();
    
    private final int defaultRequestTimeout;
    private final int defaultCooldown;
    private final int defaultWarmup;
    private final boolean cancelOnMove;
    
    public TeleportManager(AetherSuite plugin) {
        this.plugin = plugin;
        
        // Load configuration
        defaultRequestTimeout = plugin.getConfigManager().getConfig("config")
                .getInt("modules.player.teleport.request-timeout-seconds", 60);
        defaultCooldown = plugin.getConfigManager().getConfig("config")
                .getInt("modules.player.teleport.cooldown-seconds", 60);
        defaultWarmup = plugin.getConfigManager().getConfig("config")
                .getInt("modules.player.teleport.delay-seconds", 3);
        cancelOnMove = plugin.getConfigManager().getConfig("config")
                .getBoolean("modules.player.teleport.cancel-on-move", true);
        
        // Register listeners
        Bukkit.getPluginManager().registerEvents(new TeleportListener(plugin, this), plugin);
        
        // Start cleanup task for expired requests
        startCleanupTask();
    }
    
    public void sendTeleportRequest(Player sender, Player target, TeleportRequest.Type type) {
        UUID senderUuid = sender.getUniqueId();
        UUID targetUuid = target.getUniqueId();
        
        // Check if the target accepts teleport requests
        PlayerManager playerManager = plugin.getPlayerManager();
        if (!playerManager.doesAcceptTeleport(targetUuid)) {
            plugin.getMessageProvider().sendPrefixedMessage(sender, "modules.player.teleport.requests-disabled", 
                    Map.of("player", target.getName()));
            return;
        }
        
        // Check cooldown
        if (isOnCooldown(senderUuid)) {
            int remainingSeconds = getRemainingCooldown(senderUuid);
            plugin.getMessageProvider().sendPrefixedMessage(sender, "general.cooldown", 
                    Map.of("time", String.valueOf(remainingSeconds)));
            return;
        }
        
        // Check if there's already a pending request of this type
        if (hasActiveRequest(senderUuid, targetUuid, type)) {
            plugin.getMessageProvider().sendPrefixedMessage(sender, "modules.player.teleport.already-requested", 
                    Map.of("player", target.getName()));
            return;
        }
        
        // Create and store the request
        TeleportRequest request = new TeleportRequest(senderUuid, targetUuid, type);
        
        teleportRequests.computeIfAbsent(targetUuid, k -> new ConcurrentHashMap<>())
                .put(senderUuid, request);
        
        // Send messages
        String requestKey = type == TeleportRequest.Type.TPA ? "request-sent" : "request-here-sent";
        plugin.getMessageProvider().sendPrefixedMessage(sender, "modules.player.teleport." + requestKey, 
                Map.of("player", target.getName()));
        
        String receiveKey = type == TeleportRequest.Type.TPA ? "request-received" : "request-here-received";
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("player", sender.getName());
        placeholders.put("time", String.valueOf(defaultRequestTimeout));
        
        plugin.getMessageProvider().sendPrefixedMessage(target, "modules.player.teleport." + receiveKey, placeholders);
        
        // Schedule request expiration
        plugin.getScheduler().runTaskLaterAsync(() -> {
            if (hasActiveRequest(senderUuid, targetUuid, type)) {
                teleportRequests.getOrDefault(targetUuid, Collections.emptyMap()).remove(senderUuid);
                
                if (sender.isOnline() && target.isOnline()) {
                    plugin.getMessageProvider().sendPrefixedMessage(sender, "modules.player.teleport.request-expired", 
                            Map.of("player", target.getName()));
                    plugin.getMessageProvider().sendPrefixedMessage(target, "modules.player.teleport.request-expired", 
                            Map.of("player", sender.getName()));
                }
            }
        }, defaultRequestTimeout * 20L);
    }
    
    public void acceptTeleportRequest(Player player, Player requester) {
        UUID playerUuid = player.getUniqueId();
        UUID requesterUuid = requester.getUniqueId();
        
        Map<UUID, TeleportRequest> requests = teleportRequests.getOrDefault(playerUuid, Collections.emptyMap());
        TeleportRequest request = requests.get(requesterUuid);
        
        if (request == null) {
            plugin.getMessageProvider().sendPrefixedMessage(player, "modules.player.teleport.no-request", 
                    Map.of("player", requester.getName()));
            return;
        }
        
        // Remove the request
        requests.remove(requesterUuid);
        
        // Determine who teleports to whom
        Player teleporter = request.getType() == TeleportRequest.Type.TPA ? requester : player;
        Player destination = request.getType() == TeleportRequest.Type.TPA ? player : requester;
        
        // Start teleport process
        teleportPlayerWithWarmup(teleporter, destination.getLocation(), TeleportCause.COMMAND);
        
        // Send messages
        String acceptKey = request.getType() == TeleportRequest.Type.TPA ? "request-accepted" : "request-here-accepted";
        plugin.getMessageProvider().sendPrefixedMessage(player, "modules.player.teleport." + acceptKey, 
                Map.of("player", requester.getName()));
        
        String teleportingKey = request.getType() == TeleportRequest.Type.TPA ? "teleporting-to" : "teleporting-here";
        plugin.getMessageProvider().sendPrefixedMessage(requester, "modules.player.teleport." + teleportingKey, 
                Map.of("player", player.getName(), "time", String.valueOf(defaultWarmup)));
    }
    
    public void denyTeleportRequest(Player player, Player requester) {
        UUID playerUuid = player.getUniqueId();
        UUID requesterUuid = requester.getUniqueId();
        
        Map<UUID, TeleportRequest> requests = teleportRequests.getOrDefault(playerUuid, Collections.emptyMap());
        TeleportRequest request = requests.get(requesterUuid);
        
        if (request == null) {
            plugin.getMessageProvider().sendPrefixedMessage(player, "modules.player.teleport.no-request", 
                    Map.of("player", requester.getName()));
            return;
        }
        
        // Remove the request
        requests.remove(requesterUuid);
        
        // Send messages
        plugin.getMessageProvider().sendPrefixedMessage(player, "modules.player.teleport.request-denied", 
                Map.of("player", requester.getName()));
        plugin.getMessageProvider().sendPrefixedMessage(requester, "modules.player.teleport.request-denied-target", 
                Map.of("player", player.getName()));
    }
    
    public void teleportPlayerWithWarmup(Player player, Location destination, TeleportCause cause) {
        UUID playerUuid = player.getUniqueId();
        
        // Cancel any existing warmup
        cancelWarmup(playerUuid);
        
        // Get the warmup time, possibly from permissions
        int warmupTime = getWarmupTimeForPlayer(player);
        
        if (warmupTime <= 0) {
            // No warmup, teleport immediately
            teleportPlayer(player, destination, cause);
            return;
        }
        
        // Store the player's initial location for movement check
        Location initialLocation = player.getLocation();
        
        // Send warmup message
        plugin.getMessageProvider().sendPrefixedMessage(player, "modules.player.teleport.warmup", 
                Map.of("time", String.valueOf(warmupTime)));
        
        // Create warmup task
        TeleportWarmup warmup = new TeleportWarmup(
                playerUuid,
                initialLocation,
                destination,
                cause,
                warmupTime,
                plugin.getScheduler().runTaskLater(() -> {
                    if (!player.isOnline()) return;
                    
                    // Check if player moved during warmup
                    if (cancelOnMove && hasPlayerMoved(player, initialLocation)) {
                        plugin.getMessageProvider().sendPrefixedMessage(player, "modules.player.teleport.cancelled");
                        warmupTasks.remove(playerUuid);
                        return;
                    }
                    
                    // Teleport the player
                    teleportPlayer(player, destination, cause);
                    warmupTasks.remove(playerUuid);
                }, warmupTime * 20L)
        );
        
        warmupTasks.put(playerUuid, warmup);
    }
    
    public void teleportPlayer(Player player, Location destination, TeleportCause cause) {
        UUID playerUuid = player.getUniqueId();
        
        // Save the last location for /back command
        if (player.hasPermission("aethersuite.teleport.back")) {
            lastLocations.put(playerUuid, player.getLocation());
        }
        
        // Find a safe location
        Location safeLocation = SafeLocationFinder.findSafeLocation(destination);
        
        if (safeLocation == null) {
            plugin.getMessageProvider().sendPrefixedMessage(player, "modules.player.teleport.unsafe-location");
            return;
        }
        
        // Teleport player
        player.teleport(safeLocation, cause);
        
        // Update cooldown
        lastTeleport.put(playerUuid, Instant.now());
        
        // Send success message
        plugin.getMessageProvider().sendPrefixedMessage(player, "modules.player.teleport.teleported");
    }
    
    public void teleportBack(Player player) {
        UUID playerUuid = player.getUniqueId();
        Location lastLoc = lastLocations.get(playerUuid);
        
        if (lastLoc == null) {
            plugin.getMessageProvider().sendPrefixedMessage(player, "modules.player.teleport.no-back-location");
            return;
        }
        
        // Check cooldown
        if (isOnCooldown(playerUuid)) {
            int remainingSeconds = getRemainingCooldown(playerUuid);
            plugin.getMessageProvider().sendPrefixedMessage(player, "general.cooldown", 
                    Map.of("time", String.valueOf(remainingSeconds)));
            return;
        }
        
        // Teleport with warmup
        teleportPlayerWithWarmup(player, lastLoc, TeleportCause.COMMAND);
    }
    
    public void cancelWarmup(UUID playerUuid) {
        TeleportWarmup warmup = warmupTasks.remove(playerUuid);
        if (warmup != null) {
            plugin.getScheduler().cancelTask(warmup.getTaskId());
            
            Player player = Bukkit.getPlayer(playerUuid);
            if (player != null && player.isOnline()) {
                plugin.getMessageProvider().sendPrefixedMessage(player, "modules.player.teleport.warmup-cancelled");
            }
        }
    }
    
    public boolean isWarmingUp(UUID playerUuid) {
        return warmupTasks.containsKey(playerUuid);
    }
    
    public boolean isOnCooldown(UUID playerUuid) {
        if (!lastTeleport.containsKey(playerUuid)) {
            return false;
        }
        
        int cooldownTime = getCooldownTimeForPlayer(Bukkit.getPlayer(playerUuid));
        if (cooldownTime <= 0) {
            return false;
        }
        
        Instant lastTp = lastTeleport.get(playerUuid);
        Instant now = Instant.now();
        
        return now.isBefore(lastTp.plusSeconds(cooldownTime));
    }
    
    public int getRemainingCooldown(UUID playerUuid) {
        if (!lastTeleport.containsKey(playerUuid)) {
            return 0;
        }
        
        int cooldownTime = getCooldownTimeForPlayer(Bukkit.getPlayer(playerUuid));
        if (cooldownTime <= 0) {
            return 0;
        }
        
        Instant lastTp = lastTeleport.get(playerUuid);
        Instant now = Instant.now();
        
        long secondsPassed = now.getEpochSecond() - lastTp.getEpochSecond();
        return (int) Math.max(0, cooldownTime - secondsPassed);
    }
    
    private int getWarmupTimeForPlayer(Player player) {
        // Check for bypass permission
        if (player.hasPermission("aethersuite.teleport.nowarmup")) {
            return 0;
        }
        
        // Could be extended to check for tiered permissions
        return defaultWarmup;
    }
    
    private int getCooldownTimeForPlayer(Player player) {
        // Check for bypass permission
        if (player == null || player.hasPermission("aethersuite.teleport.nocooldown")) {
            return 0;
        }
        
        // Could be extended to check for tiered permissions
        return defaultCooldown;
    }
    
    private boolean hasPlayerMoved(Player player, Location initialLocation) {
        Location currentLocation = player.getLocation();
        
        // Only check x, y, z coordinates, not pitch/yaw
        return initialLocation.getWorld() != currentLocation.getWorld() ||
               initialLocation.getBlockX() != currentLocation.getBlockX() ||
               initialLocation.getBlockY() != currentLocation.getBlockY() ||
               initialLocation.getBlockZ() != currentLocation.getBlockZ();
    }
    
    private boolean hasActiveRequest(UUID senderUuid, UUID targetUuid, TeleportRequest.Type type) {
        Map<UUID, TeleportRequest> requests = teleportRequests.getOrDefault(targetUuid, Collections.emptyMap());
        TeleportRequest request = requests.get(senderUuid);
        
        return request != null && request.getType() == type;
    }
    
    private void startCleanupTask() {
        // Run every 5 minutes to clean up expired requests and cooldowns
        plugin.getScheduler().runTaskTimerAsync(() -> {
            try {
                Instant now = Instant.now();
                
                // Clean up expired requests
                for (Map<UUID, TeleportRequest> requests : teleportRequests.values()) {
                    requests.entrySet().removeIf(entry -> 
                        entry.getValue().getTimestamp().plusSeconds(defaultRequestTimeout).isBefore(now));
                }
                
                // Clean up expired cooldowns
                lastTeleport.entrySet().removeIf(entry -> 
                    entry.getValue().plusSeconds(defaultCooldown).isBefore(now));
                
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Error during teleport request cleanup", e);
            }
        }, 5 * 60 * 20L, 5 * 60 * 20L); // Every 5 minutes
    }
    
    public List<TeleportRequest> getPendingRequests(Player player) {
        UUID playerUuid = player.getUniqueId();
        Map<UUID, TeleportRequest> requests = teleportRequests.getOrDefault(playerUuid, Collections.emptyMap());
        
        return new ArrayList<>(requests.values());
    }
}