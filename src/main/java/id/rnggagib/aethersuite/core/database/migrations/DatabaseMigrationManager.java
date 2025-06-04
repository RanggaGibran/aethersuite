package id.rnggagib.aethersuite.core.database.migrations;

import id.rnggagib.aethersuite.AetherSuite;
import id.rnggagib.aethersuite.api.database.DatabaseType;
import id.rnggagib.aethersuite.api.database.Migration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;

public class DatabaseMigrationManager {
    private final AetherSuite plugin;
    private final DatabaseType databaseType;
    private final List<Migration> migrations = new ArrayList<>();
    
    public DatabaseMigrationManager(AetherSuite plugin, DatabaseType databaseType) {
        this.plugin = plugin;
        this.databaseType = databaseType;
        registerMigrations();
    }
    
    private void registerMigrations() {
        migrations.add(new V1_InitialSchema(databaseType));
        // Add more migrations as needed
    }
    
    public void runMigrations(Connection connection) {
        try {
            connection.setAutoCommit(false);
            
            int currentVersion = getCurrentVersion(connection);
            plugin.getLogger().info("Current database schema version: " + currentVersion);
            
            List<Migration> pendingMigrations = migrations.stream()
                    .filter(m -> m.getVersion() > currentVersion)
                    .sorted(Comparator.comparingInt(Migration::getVersion))
                    .toList();
            
            if (pendingMigrations.isEmpty()) {
                plugin.getLogger().info("Database schema is up to date.");
                return;
            }
            
            plugin.getLogger().info("Found " + pendingMigrations.size() + " pending migrations.");
            
            for (Migration migration : pendingMigrations) {
                plugin.getLogger().info("Applying migration V" + migration.getVersion() + ": " + migration.getDescription());
                
                try {
                    migration.migrate(connection);
                    saveVersion(connection, migration);
                    connection.commit();
                    
                    plugin.getLogger().info("Successfully applied migration V" + migration.getVersion());
                } catch (SQLException e) {
                    connection.rollback();
                    plugin.getLogger().log(Level.SEVERE, "Failed to apply migration V" + migration.getVersion(), e);
                    throw e;
                }
            }
            
            plugin.getLogger().info("Database schema is now up to date at version " + 
                pendingMigrations.get(pendingMigrations.size() - 1).getVersion());
            
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to run database migrations", e);
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException ignored) {}
        }
    }
    
    private int getCurrentVersion(Connection connection) throws SQLException {
        try {
            String sql = "SELECT MAX(version) FROM aether_schema_version";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                ResultSet rs = statement.executeQuery();
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            return 0;
        }
        
        return 0;
    }
    
    private void saveVersion(Connection connection, Migration migration) throws SQLException {
        String sql = "INSERT INTO aether_schema_version (version, description) VALUES (?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, migration.getVersion());
            statement.setString(2, migration.getDescription());
            statement.executeUpdate();
        }
    }
}