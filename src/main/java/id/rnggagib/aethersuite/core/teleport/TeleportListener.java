package id.rnggagib.aethersuite.core.teleport;

import id.rnggagib.aethersuite.AetherSuite;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TeleportListener implements Listener {
    private final AetherSuite plugin;
    private final TeleportManager teleportManager;
    private final Map<UUID, Location> deathLocations = new ConcurrentHashMap<>();
    
    public TeleportListener(AetherSuite plugin, TeleportManager teleportManager) {
        this.plugin = plugin;
        this.teleportManager = teleportManager;
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        // Only check actual position changes, not just head movement
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
            event.getFrom().getBlockY() == event.getTo().getBlockY() &&
            event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }
        
        Player player = event.getPlayer();
        UUID playerUuid = player.getUniqueId();
        
        // Cancel warmup if player is moving
        if (teleportManager.isWarmingUp(playerUuid)) {
            teleportManager.cancelWarmup(playerUuid);
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID playerUuid = player.getUniqueId();
        
        // Cancel warmup if player disconnects
        if (teleportManager.isWarmingUp(playerUuid)) {
            teleportManager.cancelWarmup(playerUuid);
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        
        // Store the death location for /back command if player has permission
        if (player.hasPermission("aethersuite.teleport.back")) {
            deathLocations.put(player.getUniqueId(), player.getLocation());
            
            // Inform the player about /back
            plugin.getScheduler().runTaskLater(() -> {
                if (player.isOnline()) {
                    plugin.getMessageProvider().sendPrefixedMessage(player, "modules.player.teleport.death-back-hint");
                }
            }, 5L);
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        // We're not storing regular teleports for back command here as that's handled
        // directly in the teleport manager. This is just a hook for future extensions.
    }
    
    public Location getDeathLocation(UUID playerUuid) {
        return deathLocations.get(playerUuid);
    }
    
    public void clearDeathLocation(UUID playerUuid) {
        deathLocations.remove(playerUuid);
    }
}