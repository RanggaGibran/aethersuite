package id.rnggagib.aethersuite.core.database.dao;

import id.rnggagib.aethersuite.api.database.DatabaseManager;
import id.rnggagib.aethersuite.api.database.dao.HomeDao;
import id.rnggagib.aethersuite.api.database.DatabaseType;
import id.rnggagib.aethersuite.api.home.Home;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class SQLHomeDao extends AbstractDao implements HomeDao {
    
    public SQLHomeDao(DatabaseManager databaseManager) {
        super(databaseManager);
    }
    
    @Override
    public CompletableFuture<Optional<Home>> getHome(UUID playerUuid, String name) {
        return executeAsync(connection -> {
            String sql = "SELECT h.id, p.uuid as player_uuid, h.name, h.world, h.x, h.y, h.z, h.yaw, h.pitch, " +
                         "h.created_at, h.updated_at " +
                         "FROM aether_homes h " +
                         "JOIN aether_players p ON h.player_id = p.id " +
                         "WHERE p.uuid = ? AND h.name = ?";
            
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                if (databaseManager.getDatabaseType() == DatabaseType.MYSQL) {
                    statement.setBytes(1, uuidToBytes(playerUuid));
                } else {
                    statement.setString(1, playerUuid.toString());
                }
                statement.setString(2, name);
                
                ResultSet rs = statement.executeQuery();
                if (rs.next()) {
                    return Optional.of(extractHome(rs));
                }
            }
            
            return Optional.empty();
        });
    }
    
    @Override
    public CompletableFuture<Optional<Home>> getHome(int id) {
        return executeAsync(connection -> {
            String sql = "SELECT h.id, p.uuid as player_uuid, h.name, h.world, h.x, h.y, h.z, h.yaw, h.pitch, " +
                         "h.created_at, h.updated_at " +
                         "FROM aether_homes h " +
                         "JOIN aether_players p ON h.player_id = p.id " +
                         "WHERE h.id = ?";
            
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, id);
                
                ResultSet rs = statement.executeQuery();
                if (rs.next()) {
                    return Optional.of(extractHome(rs));
                }
            }
            
            return Optional.empty();
        });
    }
    
    @Override
    public CompletableFuture<List<Home>> getHomes(UUID playerUuid) {
        return executeAsync(connection -> {
            String sql = "SELECT h.id, p.uuid as player_uuid, h.name, h.world, h.x, h.y, h.z, h.yaw, h.pitch, " +
                         "h.created_at, h.updated_at " +
                         "FROM aether_homes h " +
                         "JOIN aether_players p ON h.player_id = p.id " +
                         "WHERE p.uuid = ? " +
                         "ORDER BY h.name ASC";
            
            List<Home> homes = new ArrayList<>();
            
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                if (databaseManager.getDatabaseType() == DatabaseType.MYSQL) {
                    statement.setBytes(1, uuidToBytes(playerUuid));
                } else {
                    statement.setString(1, playerUuid.toString());
                }
                
                ResultSet rs = statement.executeQuery();
                while (rs.next()) {
                    homes.add(extractHome(rs));
                }
            }
            
            return homes;
        });
    }
    
    @Override
    public CompletableFuture<Integer> countHomes(UUID playerUuid) {
        return executeAsync(connection -> {
            String sql = "SELECT COUNT(*) FROM aether_homes h " +
                         "JOIN aether_players p ON h.player_id = p.id " +
                         "WHERE p.uuid = ?";
            
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                if (databaseManager.getDatabaseType() == DatabaseType.MYSQL) {
                    statement.setBytes(1, uuidToBytes(playerUuid));
                } else {
                    statement.setString(1, playerUuid.toString());
                }
                
                ResultSet rs = statement.executeQuery();
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            
            return 0;
        });
    }
    
    @Override
    public CompletableFuture<Integer> createHome(UUID playerUuid, String name, Location location) {
        return executeAsync(connection -> {
            // First get the player_id
            String playerIdSql = "SELECT id FROM aether_players WHERE uuid = ?";
            int playerId;
            
            try (PreparedStatement statement = connection.prepareStatement(playerIdSql)) {
                if (databaseManager.getDatabaseType() == DatabaseType.MYSQL) {
                    statement.setBytes(1, uuidToBytes(playerUuid));
                } else {
                    statement.setString(1, playerUuid.toString());
                }
                
                ResultSet rs = statement.executeQuery();
                if (rs.next()) {
                    playerId = rs.getInt("id");
                } else {
                    throw new SQLException("Player not found: " + playerUuid);
                }
            }
            
            // Now insert the home
            String sql = "INSERT INTO aether_homes (player_id, name, world, x, y, z, yaw, pitch) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            
            try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                statement.setInt(1, playerId);
                statement.setString(2, name);
                statement.setString(3, location.getWorld().getName());
                statement.setDouble(4, location.getX());
                statement.setDouble(5, location.getY());
                statement.setDouble(6, location.getZ());
                statement.setFloat(7, location.getYaw());
                statement.setFloat(8, location.getPitch());
                
                statement.executeUpdate();
                
                ResultSet generatedKeys = statement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating home failed, no ID obtained.");
                }
            }
        });
    }
    
    @Override
    public CompletableFuture<Boolean> updateHome(UUID playerUuid, String name, Location location) {
        return executeAsync(connection -> { // Changed from executeUpdateAsync
            String sql = "UPDATE aether_homes SET " +
                         "world = ?, x = ?, y = ?, z = ?, yaw = ?, pitch = ?, updated_at = CURRENT_TIMESTAMP " +
                         "WHERE player_id = (SELECT id FROM aether_players WHERE uuid = ?) AND name = ?";
            
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, location.getWorld().getName());
                statement.setDouble(2, location.getX());
                statement.setDouble(3, location.getY());
                statement.setDouble(4, location.getZ());
                statement.setFloat(5, location.getYaw());
                statement.setFloat(6, location.getPitch());
                
                if (databaseManager.getDatabaseType() == DatabaseType.MYSQL) {
                    statement.setBytes(7, uuidToBytes(playerUuid));
                } else {
                    statement.setString(7, playerUuid.toString());
                }
                statement.setString(8, name);
                
                return statement.executeUpdate() > 0;
            }
        });
    }
    
    @Override
    public CompletableFuture<Boolean> deleteHome(UUID playerUuid, String name) {
        return executeAsync(connection -> { // Changed from executeUpdateAsync
            String sql = "DELETE FROM aether_homes " +
                         "WHERE player_id = (SELECT id FROM aether_players WHERE uuid = ?) AND name = ?";
            
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                if (databaseManager.getDatabaseType() == DatabaseType.MYSQL) {
                    statement.setBytes(1, uuidToBytes(playerUuid));
                } else {
                    statement.setString(1, playerUuid.toString());
                }
                statement.setString(2, name);
                
                return statement.executeUpdate() > 0;
            }
        });
    }
    
    @Override
    public CompletableFuture<Boolean> deleteHome(int id) {
        return executeAsync(connection -> { // Changed from executeUpdateAsync
            String sql = "DELETE FROM aether_homes WHERE id = ?";
            
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, id);
                
                return statement.executeUpdate() > 0;
            }
        });
    }
    
    @Override
    public CompletableFuture<Boolean> homeExists(UUID playerUuid, String name) {
        return executeAsync(connection -> {
            String sql = "SELECT 1 FROM aether_homes h " +
                         "JOIN aether_players p ON h.player_id = p.id " +
                         "WHERE p.uuid = ? AND h.name = ?";
            
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                if (databaseManager.getDatabaseType() == DatabaseType.MYSQL) {
                    statement.setBytes(1, uuidToBytes(playerUuid));
                } else {
                    statement.setString(1, playerUuid.toString());
                }
                statement.setString(2, name);
                
                ResultSet rs = statement.executeQuery();
                return rs.next();
            }
        });
    }
    
    private Home extractHome(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        UUID playerUuid;
        
        if (databaseManager.getDatabaseType() == DatabaseType.MYSQL) {
            playerUuid = bytesToUuid(rs.getBytes("player_uuid"));
        } else {
            playerUuid = UUID.fromString(rs.getString("player_uuid"));
        }
        
        String name = rs.getString("name");
        String worldName = rs.getString("world");
        double x = rs.getDouble("x");
        double y = rs.getDouble("y");
        double z = rs.getDouble("z");
        float yaw = rs.getFloat("yaw");
        float pitch = rs.getFloat("pitch");
        
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            throw new SQLException("World not found: " + worldName);
        }
        
        Location location = new Location(world, x, y, z, yaw, pitch);
        
        Instant createdAt = rs.getTimestamp("created_at").toInstant();
        Instant updatedAt = rs.getTimestamp("updated_at").toInstant();
        
        return new Home(id, playerUuid, name, location, createdAt, updatedAt);
    }
}