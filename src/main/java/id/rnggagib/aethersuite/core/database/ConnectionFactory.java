package id.rnggagib.aethersuite.core.database;

import id.rnggagib.aethersuite.AetherSuite;
import id.rnggagib.aethersuite.api.database.ConnectionProvider;
import id.rnggagib.aethersuite.api.database.DatabaseType;
import id.rnggagib.aethersuite.core.database.providers.MySQLConnectionProvider;
import id.rnggagib.aethersuite.core.database.providers.SQLiteConnectionProvider;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;

public class ConnectionFactory {
    private final AetherSuite plugin;
    
    public ConnectionFactory(AetherSuite plugin) {
        this.plugin = plugin;
    }
    
    public ConnectionProvider createConnectionProvider() {
        ConfigurationSection dbConfig = plugin.getConfigManager().getConfig("config").getConfigurationSection("database");
        
        if (dbConfig == null) {
            plugin.getLogger().warning("Database configuration not found. Using default SQLite configuration.");
            return createSQLiteProvider("aethersuite.db");
        }
        
        String dbTypeStr = dbConfig.getString("type", "SQLITE").toUpperCase();
        DatabaseType dbType;
        
        try {
            dbType = DatabaseType.valueOf(dbTypeStr);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid database type: " + dbTypeStr + ". Using SQLite instead.");
            dbType = DatabaseType.SQLITE;
        }
        
        switch (dbType) {
            case MYSQL:
                ConfigurationSection mysqlConfig = dbConfig.getConfigurationSection("mysql");
                if (mysqlConfig == null) {
                    plugin.getLogger().warning("MySQL configuration not found. Using default SQLite configuration.");
                    return createSQLiteProvider("aethersuite.db");
                }
                return new MySQLConnectionProvider(mysqlConfig);
                
            case SQLITE:
            default:
                ConfigurationSection sqliteConfig = dbConfig.getConfigurationSection("sqlite");
                String filename = sqliteConfig != null ? sqliteConfig.getString("filename", "aethersuite.db") : "aethersuite.db";
                return createSQLiteProvider(filename);
        }
    }
    
    private SQLiteConnectionProvider createSQLiteProvider(String filename) {
        return new SQLiteConnectionProvider(plugin.getDataFolder(), filename);
    }
}