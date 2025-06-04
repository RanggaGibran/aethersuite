package id.rnggagib.aethersuite.api.home;

import org.bukkit.Location;

import java.time.Instant;
import java.util.UUID;

public class Home {
    private final int id;
    private final UUID playerUuid;
    private final String name;
    private Location location;
    private final Instant createdAt;
    private Instant updatedAt;
    
    public Home(int id, UUID playerUuid, String name, Location location, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.playerUuid = playerUuid;
        this.name = name;
        this.location = location;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    public int getId() {
        return id;
    }
    
    public UUID getPlayerUuid() {
        return playerUuid;
    }
    
    public String getName() {
        return name;
    }
    
    public Location getLocation() {
        return location;
    }
    
    public void setLocation(Location location) {
        this.location = location;
        this.updatedAt = Instant.now();
    }
    
    public Instant getCreatedAt() {
        return createdAt;
    }
    
    public Instant getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}