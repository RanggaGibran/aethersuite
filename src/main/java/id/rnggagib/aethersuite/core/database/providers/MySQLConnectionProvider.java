package id.rnggagib.aethersuite.core.database.providers;

import com.zaxxer.hikari.HikariConfig;
import id.rnggagib.aethersuite.api.database.DatabaseType;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Properties;

public class MySQLConnectionProvider extends HikariConnectionProvider {
    
    public MySQLConnectionProvider(ConfigurationSection config) {
        super(config.getString("database", "aethersuite"), 
              DatabaseType.MYSQL, 
              createConfig(config));
    }
    
    private static HikariConfig createConfig(ConfigurationSection config) {
        String host = config.getString("host", "localhost");
        int port = config.getInt("port", 3306);
        String database = config.getString("database", "aethersuite");
        String username = config.getString("username", "root");
        String password = config.getString("password", "");
        int poolSize = config.getInt("pool-size", 10);
        long maxLifetime = config.getLong("max-lifetime", 1800000);
        long connectionTimeout = config.getLong("connection-timeout", 5000);
        
        String jdbcUrl = "jdbc:mysql://" + host + ":" + port + "/" + database;
        
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(jdbcUrl);
        hikariConfig.setUsername(username);
        hikariConfig.setPassword(password);
        hikariConfig.setMaximumPoolSize(poolSize);
        hikariConfig.setMaxLifetime(maxLifetime);
        hikariConfig.setConnectionTimeout(connectionTimeout);
        hikariConfig.setPoolName("AetherSuite-MySQL");
        
        // MySQL specific properties
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        hikariConfig.addDataSourceProperty("useServerPrepStmts", "true");
        
        // Add any additional properties from config
        ConfigurationSection properties = config.getConfigurationSection("properties");
        if (properties != null) {
            for (String key : properties.getKeys(false)) {
                String value = properties.getString(key);
                if (value != null) {
                    hikariConfig.addDataSourceProperty(key, value);
                }
            }
        }
        
        return hikariConfig;
    }
}