package id.rnggagib.aethersuite.core.database;

import id.rnggagib.aethersuite.AetherSuite;
import id.rnggagib.aethersuite.api.database.ConnectionProvider;
import id.rnggagib.aethersuite.api.database.DatabaseManager;
import id.rnggagib.aethersuite.api.database.DatabaseType;
import id.rnggagib.aethersuite.api.database.dao.PlayerDataDao;
import id.rnggagib.aethersuite.core.database.dao.SQLPlayerDataDao;
import id.rnggagib.aethersuite.core.database.migrations.DatabaseMigrationManager;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;

public class SimpleDatabaseManager implements DatabaseManager {
    private final AetherSuite plugin;
    private ConnectionProvider connectionProvider;
    private DatabaseMigrationManager migrationManager;
    private PlayerDataDao playerDataDao;
    
    public SimpleDatabaseManager(AetherSuite plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public void initialize() {
        try {
            ConnectionFactory factory = new ConnectionFactory(plugin);
            connectionProvider = factory.createConnectionProvider();
            
            plugin.getLogger().info("Initializing database connection of type: " + connectionProvider.getType());
            
            migrationManager = new DatabaseMigrationManager(plugin, connectionProvider.getType());
            runMigrations();
            
            playerDataDao = new SQLPlayerDataDao(this);
            
            plugin.getLogger().info("Database connection initialized successfully.");
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to initialize database connection", e);
        }
    }
    
    @Override
    public void shutdown() {
        if (connectionProvider != null && !connectionProvider.isClosed()) {
            connectionProvider.close();
            plugin.getLogger().info("Database connection closed.");
        }
    }
    
    @Override
    public Connection getConnection() throws SQLException {
        if (connectionProvider == null || connectionProvider.isClosed()) {
            throw new SQLException("Database connection is not initialized or has been closed.");
        }
        return connectionProvider.getConnection();
    }
    
    @Override
    public boolean isConnected() {
        return connectionProvider != null && !connectionProvider.isClosed();
    }
    
    @Override
    public DatabaseType getDatabaseType() {
        return connectionProvider != null ? connectionProvider.getType() : DatabaseType.SQLITE;
    }
    
    @Override
    public void runMigrations() {
        if (migrationManager == null) {
            plugin.getLogger().warning("Migration manager is not initialized.");
            return;
        }
        
        try (Connection connection = getConnection()) {
            migrationManager.runMigrations(connection);
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to run database migrations", e);
        }
    }
    
    @Override
    public PlayerDataDao getPlayerDataDao() {
        return playerDataDao;
    }
}