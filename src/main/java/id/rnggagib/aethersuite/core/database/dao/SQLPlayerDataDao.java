package id.rnggagib.aethersuite.core.database.dao;

import id.rnggagib.aethersuite.api.database.DatabaseManager;
import id.rnggagib.aethersuite.api.database.DatabaseType;
import id.rnggagib.aethersuite.api.database.dao.PlayerDataDao;
import id.rnggagib.aethersuite.api.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.sql.*;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class SQLPlayerDataDao extends AbstractDao implements PlayerDataDao {
    
    public SQLPlayerDataDao(DatabaseManager databaseManager) {
        super(databaseManager);
    }
    
    @Override
    public CompletableFuture<Optional<PlayerData>> getPlayerData(UUID uuid) {
        return executeAsync(connection -> {
            String sql = "SELECT * FROM aether_players WHERE uuid = ?";
            
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                if (databaseManager.getDatabaseType() == DatabaseType.MYSQL) {
                    statement.setBytes(1, uuidToBytes(uuid));
                } else {
                    statement.setString(1, uuid.toString());
                }
                
                ResultSet rs = statement.executeQuery();
                if (rs.next()) {
                    return Optional.of(extractPlayerData(rs));
                }
            }
            
            return Optional.empty();
        });
    }
    
    @Override
    public CompletableFuture<Optional<PlayerData>> getPlayerData(String username) {
        return executeAsync(connection -> {
            String sql = "SELECT * FROM aether_players WHERE username = ?";
            
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, username);
                
                ResultSet rs = statement.executeQuery();
                if (rs.next()) {
                    return Optional.of(extractPlayerData(rs));
                }
            }
            
            return Optional.empty();
        });
    }
    
    @Override
    public CompletableFuture<Void> savePlayerData(PlayerData playerData) {
        return executeUpdateAsync(connection -> {
            String sql = "UPDATE aether_players SET " +
                         "username = ?, " +
                         "last_login = ?, " +
                         "balance = ?, " +
                         "last_location = ?, " +
                         "accept_teleport = ?, " +
                         "language = ?, " +
                         "settings = ? " +
                         "WHERE uuid = ?";
            
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, playerData.getUsername());
                statement.setTimestamp(2, Timestamp.from(playerData.getLastLogin()));
                statement.setDouble(3, playerData.getBalance());
                statement.setString(4, locationToString(playerData.getLastLocation()));
                statement.setBoolean(5, playerData.isAcceptTeleport());
                statement.setString(6, playerData.getLanguage());
                statement.setString(7, playerData.getSettings());
                
                if (databaseManager.getDatabaseType() == DatabaseType.MYSQL) {
                    statement.setBytes(8, uuidToBytes(playerData.getUuid()));
                } else {
                    statement.setString(8, playerData.getUuid().toString());
                }
                
                statement.executeUpdate();
            }
        });
    }
    
    @Override
    public CompletableFuture<Boolean> playerExists(UUID uuid) {
        return executeAsync(connection -> {
            String sql = "SELECT 1 FROM aether_players WHERE uuid = ?";
            
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                if (databaseManager.getDatabaseType() == DatabaseType.MYSQL) {
                    statement.setBytes(1, uuidToBytes(uuid));
                } else {
                    statement.setString(1, uuid.toString());
                }
                
                ResultSet rs = statement.executeQuery();
                return rs.next();
            }
        });
    }
    
    @Override
    public CompletableFuture<Void> createPlayerData(UUID uuid, String username) {
        return executeUpdateAsync(connection -> {
            String sql = "INSERT INTO aether_players (uuid, username) VALUES (?, ?)";
            
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                if (databaseManager.getDatabaseType() == DatabaseType.MYSQL) {
                    statement.setBytes(1, uuidToBytes(uuid));
                } else {
                    statement.setString(1, uuid.toString());
                }
                
                statement.setString(2, username);
                
                statement.executeUpdate();
            }
        });
    }
    
    @Override
    public CompletableFuture<Void> updateLastLogin(UUID uuid) {
        return executeUpdateAsync(connection -> {
            String sql = "UPDATE aether_players SET last_login = CURRENT_TIMESTAMP WHERE uuid = ?";
            
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                if (databaseManager.getDatabaseType() == DatabaseType.MYSQL) {
                    statement.setBytes(1, uuidToBytes(uuid));
                } else {
                    statement.setString(1, uuid.toString());
                }
                
                statement.executeUpdate();
            }
        });
    }
    
    @Override
    public CompletableFuture<Void> updateBalance(UUID uuid, double newBalance) {
        return executeUpdateAsync(connection -> {
            String sql = "UPDATE aether_players SET balance = ? WHERE uuid = ?";
            
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setDouble(1, newBalance);
                
                if (databaseManager.getDatabaseType() == DatabaseType.MYSQL) {
                    statement.setBytes(2, uuidToBytes(uuid));
                } else {
                    statement.setString(2, uuid.toString());
                }
                
                statement.executeUpdate();
            }
        });
    }
    
    @Override
    public CompletableFuture<Double> getBalance(UUID uuid) {
        return executeAsync(connection -> {
            String sql = "SELECT balance FROM aether_players WHERE uuid = ?";
            
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                if (databaseManager.getDatabaseType() == DatabaseType.MYSQL) {
                    statement.setBytes(1, uuidToBytes(uuid));
                } else {
                    statement.setString(1, uuid.toString());
                }
                
                ResultSet rs = statement.executeQuery();
                if (rs.next()) {
                    return rs.getDouble("balance");
                }
                
                return 0.0;
            }
        });
    }
    
    private PlayerData extractPlayerData(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        UUID uuid;
        
        if (databaseManager.getDatabaseType() == DatabaseType.MYSQL) {
            uuid = bytesToUuid(rs.getBytes("uuid"));
        } else {
            uuid = UUID.fromString(rs.getString("uuid"));
        }
        
        String username = rs.getString("username");
        Instant lastLogin = rs.getTimestamp("last_login").toInstant();
        Instant firstJoin = rs.getTimestamp("first_join").toInstant();
        double balance = rs.getDouble("balance");
        Location lastLocation = stringToLocation(rs.getString("last_location"));
        boolean acceptTeleport = rs.getBoolean("accept_teleport");
        String language = rs.getString("language");
        String settings = rs.getString("settings");
        
        return new PlayerData(id, uuid, username, lastLogin, firstJoin, balance, 
                             lastLocation, acceptTeleport, language, settings);
    }
    
    private Location stringToLocation(String locationStr) {
        if (locationStr == null || locationStr.isEmpty()) {
            return null;
        }
        
        String[] parts = locationStr.split(":");
        if (parts.length != 6) {
            return null;
        }
        
        World world = Bukkit.getWorld(parts[0]);
        if (world == null) {
            return null;
        }
        
        try {
            double x = Double.parseDouble(parts[1]);
            double y = Double.parseDouble(parts[2]);
            double z = Double.parseDouble(parts[3]);
            float yaw = Float.parseFloat(parts[4]);
            float pitch = Float.parseFloat(parts[5]);
            
            return new Location(world, x, y, z, yaw, pitch);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}