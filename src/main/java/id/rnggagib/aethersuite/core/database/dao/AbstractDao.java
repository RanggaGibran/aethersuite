package id.rnggagib.aethersuite.core.database.dao;

import id.rnggagib.aethersuite.api.database.DatabaseManager;
import org.bukkit.Location;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public abstract class AbstractDao {
    protected final DatabaseManager databaseManager;
    
    protected AbstractDao(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }
    
    protected <T> CompletableFuture<T> executeAsync(SQLFunction<T> function) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = databaseManager.getConnection()) {
                return function.apply(connection);
            } catch (SQLException e) {
                throw new RuntimeException("Database error", e);
            }
        });
    }
    
    protected CompletableFuture<Void> executeUpdateAsync(SQLConsumer consumer) {
        return CompletableFuture.runAsync(() -> {
            try (Connection connection = databaseManager.getConnection()) {
                consumer.accept(connection);
            } catch (SQLException e) {
                throw new RuntimeException("Database error", e);
            }
        });
    }
    
    protected byte[] uuidToBytes(UUID uuid) {
        long mostSigBits = uuid.getMostSignificantBits();
        long leastSigBits = uuid.getLeastSignificantBits();
        
        byte[] bytes = new byte[16];
        
        for (int i = 0; i < 8; i++) {
            bytes[i] = (byte) (mostSigBits >>> (8 * (7 - i)));
            bytes[8 + i] = (byte) (leastSigBits >>> (8 * (7 - i)));
        }
        
        return bytes;
    }
    
    protected UUID bytesToUuid(byte[] bytes) {
        if (bytes == null || bytes.length != 16) {
            return null;
        }
        
        long mostSigBits = 0;
        long leastSigBits = 0;
        
        for (int i = 0; i < 8; i++) {
            mostSigBits = (mostSigBits << 8) | (bytes[i] & 0xff);
            leastSigBits = (leastSigBits << 8) | (bytes[8 + i] & 0xff);
        }
        
        return new UUID(mostSigBits, leastSigBits);
    }
    
    protected String locationToString(Location location) {
        if (location == null) {
            return null;
        }
        
        return location.getWorld().getName() + ":" +
               location.getX() + ":" +
               location.getY() + ":" +
               location.getZ() + ":" +
               location.getYaw() + ":" +
               location.getPitch();
    }
    
    @FunctionalInterface
    protected interface SQLFunction<T> {
        T apply(Connection connection) throws SQLException;
    }
    
    @FunctionalInterface
    protected interface SQLConsumer {
        void accept(Connection connection) throws SQLException;
    }
}