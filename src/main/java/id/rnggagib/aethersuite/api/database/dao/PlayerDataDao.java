package id.rnggagib.aethersuite.api.database.dao;

import id.rnggagib.aethersuite.api.player.PlayerData;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface PlayerDataDao {
    CompletableFuture<Optional<PlayerData>> getPlayerData(UUID uuid);
    
    CompletableFuture<Optional<PlayerData>> getPlayerData(String username);
    
    CompletableFuture<Void> savePlayerData(PlayerData playerData);
    
    CompletableFuture<Boolean> playerExists(UUID uuid);
    
    CompletableFuture<Void> createPlayerData(UUID uuid, String username);
    
    CompletableFuture<Void> updateLastLogin(UUID uuid);
    
    CompletableFuture<Void> updateBalance(UUID uuid, double newBalance);
    
    CompletableFuture<Double> getBalance(UUID uuid);
}