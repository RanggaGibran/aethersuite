package id.rnggagib.aethersuite.api.database;

import java.sql.Connection;
import java.sql.SQLException;

public interface ConnectionProvider {
    Connection getConnection() throws SQLException;
    
    void close();
    
    boolean isClosed();
    
    String getDatabaseName();
    
    DatabaseType getType();
}