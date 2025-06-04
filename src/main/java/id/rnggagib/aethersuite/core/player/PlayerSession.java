package id.rnggagib.aethersuite.core.player;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerSession {
    private final UUID playerUuid;
    private final Instant sessionStart;
    private final String ipAddress;
    private final String clientLocale;
    private final Map<String, Object> sessionData = new HashMap<>();
    private boolean afk = false;
    private Instant lastActivity;
    private org.bukkit.Location lastLocation;
    
    public PlayerSession(UUID playerUuid, Instant sessionStart, String ipAddress, String clientLocale) {
        this.playerUuid = playerUuid;
        this.sessionStart = sessionStart;
        this.ipAddress = ipAddress;
        this.clientLocale = clientLocale;
        this.lastActivity = sessionStart;
    }
    
    public UUID getPlayerUuid() {
        return playerUuid;
    }
    
    public Instant getSessionStart() {
        return sessionStart;
    }
    
    public Duration getSessionDuration() {
        return Duration.between(sessionStart, Instant.now());
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public String getClientLocale() {
        return clientLocale;
    }
    
    public void setAfk(boolean afk) {
        this.afk = afk;
    }
    
    public boolean isAfk() {
        return afk;
    }
    
    public Instant getLastActivity() {
        return lastActivity;
    }
    
    public void updateActivity() {
        this.lastActivity = Instant.now();
    }
    
    public Duration getIdleTime() {
        return Duration.between(lastActivity, Instant.now());
    }
    
    public void setSessionData(String key, Object value) {
        sessionData.put(key, value);
    }
    
    public Object getSessionData(String key) {
        return sessionData.get(key);
    }
    
    public void removeSessionData(String key) {
        sessionData.remove(key);
    }
    
    public boolean hasSessionData(String key) {
        return sessionData.containsKey(key);
    }
    
    public void setLastLocation(org.bukkit.Location location) {
        this.lastLocation = location;
    }
    
    public org.bukkit.Location getLastLocation() {
        return lastLocation;
    }
}