package id.rnggagib.aethersuite.api.database;

import id.rnggagib.aethersuite.api.database.dao.HomeDao;
import id.rnggagib.aethersuite.api.database.dao.PlayerDataDao;
import java.sql.Connection;
import java.sql.SQLException;

public interface DatabaseManager {
    void initialize();
    
    void shutdown();
    
    Connection getConnection() throws SQLException;
    
    boolean isConnected();
    
    DatabaseType getDatabaseType();
    
    void runMigrations();
    
    PlayerDataDao getPlayerDataDao();
    
    HomeDao getHomeDao();
}