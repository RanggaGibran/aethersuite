package id.rnggagib.aethersuite.api.database;

import java.sql.Connection;
import java.sql.SQLException;

public interface Migration {
    int getVersion();
    
    String getDescription();
    
    void migrate(Connection connection) throws SQLException;
}