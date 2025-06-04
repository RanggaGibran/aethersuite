package id.rnggagib.aethersuite.core.database.providers;

import com.zaxxer.hikari.HikariConfig;
import id.rnggagib.aethersuite.api.database.DatabaseType;

import java.io.File;

public class SQLiteConnectionProvider extends HikariConnectionProvider {
    
    public SQLiteConnectionProvider(File dataFolder, String filename) {
        super(filename, DatabaseType.SQLITE, createConfig(dataFolder, filename));
    }
    
    private static HikariConfig createConfig(File dataFolder, String filename) {
        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            throw new RuntimeException("Failed to create data folder: " + dataFolder.getAbsolutePath());
        }
        
        File databaseFile = new File(dataFolder, filename);
        String jdbcUrl = "jdbc:sqlite:" + databaseFile.getAbsolutePath();
        
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setMaximumPoolSize(1); // SQLite only supports one connection
        config.setConnectionTestQuery("SELECT 1");
        
        // SQLite specific properties
        config.addDataSourceProperty("foreign_keys", "true");
        config.addDataSourceProperty("journal_mode", "WAL");
        config.addDataSourceProperty("synchronous", "NORMAL");
        
        return config;
    }
}