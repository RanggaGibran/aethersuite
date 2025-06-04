package id.rnggagib.aethersuite.api.database.dao;

import id.rnggagib.aethersuite.api.home.Home;
import org.bukkit.Location;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface HomeDao {
    CompletableFuture<Optional<Home>> getHome(UUID playerUuid, String name);
    
    CompletableFuture<Optional<Home>> getHome(int id);
    
    CompletableFuture<List<Home>> getHomes(UUID playerUuid);
    
    CompletableFuture<Integer> countHomes(UUID playerUuid);
    
    CompletableFuture<Integer> createHome(UUID playerUuid, String name, Location location);
    
    CompletableFuture<Boolean> updateHome(UUID playerUuid, String name, Location location);
    
    CompletableFuture<Boolean> deleteHome(UUID playerUuid, String name);
    
    CompletableFuture<Boolean> deleteHome(int id);
    
    CompletableFuture<Boolean> homeExists(UUID playerUuid, String name);
}