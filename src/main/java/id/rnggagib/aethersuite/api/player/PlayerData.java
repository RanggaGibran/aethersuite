package id.rnggagib.aethersuite.api.player;

import org.bukkit.Location;

import java.time.Instant;
import java.util.UUID;

public class PlayerData {
    private final int id;
    private final UUID uuid;
    private String username;
    private Instant lastLogin;
    private Instant firstJoin;
    private double balance;
    private Location lastLocation;
    private boolean acceptTeleport;
    private String language;
    private String settings;
    
    public PlayerData(int id, UUID uuid, String username, Instant lastLogin, Instant firstJoin, 
                      double balance, Location lastLocation, boolean acceptTeleport, 
                      String language, String settings) {
        this.id = id;
        this.uuid = uuid;
        this.username = username;
        this.lastLogin = lastLogin;
        this.firstJoin = firstJoin;
        this.balance = balance;
        this.lastLocation = lastLocation;
        this.acceptTeleport = acceptTeleport;
        this.language = language;
        this.settings = settings;
    }
    
    public int getId() {
        return id;
    }
    
    public UUID getUuid() {
        return uuid;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public Instant getLastLogin() {
        return lastLogin;
    }
    
    public void setLastLogin(Instant lastLogin) {
        this.lastLogin = lastLogin;
    }
    
    public Instant getFirstJoin() {
        return firstJoin;
    }
    
    public double getBalance() {
        return balance;
    }
    
    public void setBalance(double balance) {
        this.balance = balance;
    }
    
    public Location getLastLocation() {
        return lastLocation;
    }
    
    public void setLastLocation(Location lastLocation) {
        this.lastLocation = lastLocation;
    }
    
    public boolean isAcceptTeleport() {
        return acceptTeleport;
    }
    
    public void setAcceptTeleport(boolean acceptTeleport) {
        this.acceptTeleport = acceptTeleport;
    }
    
    public String getLanguage() {
        return language;
    }
    
    public void setLanguage(String language) {
        this.language = language;
    }
    
    public String getSettings() {
        return settings;
    }
    
    public void setSettings(String settings) {
        this.settings = settings;
    }
}