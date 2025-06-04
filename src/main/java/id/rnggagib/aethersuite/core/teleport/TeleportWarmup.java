package id.rnggagib.aethersuite.core.teleport;

import org.bukkit.Location;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import java.util.UUID;

public class TeleportWarmup {
    private final UUID playerUuid;
    private final Location initialLocation;
    private final Location destination;
    private final TeleportCause cause;
    private final int warmupTime;
    private final int taskId;
    
    public TeleportWarmup(UUID playerUuid, Location initialLocation, Location destination, 
                           TeleportCause cause, int warmupTime, int taskId) {
        this.playerUuid = playerUuid;
        this.initialLocation = initialLocation;
        this.destination = destination;
        this.cause = cause;
        this.warmupTime = warmupTime;
        this.taskId = taskId;
    }
    
    public UUID getPlayerUuid() {
        return playerUuid;
    }
    
    public Location getInitialLocation() {
        return initialLocation;
    }
    
    public Location getDestination() {
        return destination;
    }
    
    public TeleportCause getCause() {
        return cause;
    }
    
    public int getWarmupTime() {
        return warmupTime;
    }
    
    public int getTaskId() {
        return taskId;
    }
}