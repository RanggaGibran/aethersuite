package id.rnggagib.aethersuite.core.database.migrations;

import id.rnggagib.aethersuite.api.database.DatabaseType;

import java.sql.Connection;
import java.sql.SQLException;

public class V1_InitialSchema extends BaseMigration {
    private final DatabaseType databaseType;
    
    public V1_InitialSchema(DatabaseType databaseType) {
        super(1, "Initial schema creation");
        this.databaseType = databaseType;
    }
    
    @Override
    public void migrate(Connection connection) throws SQLException {
        createVersionTable(connection);
        createPlayersTable(connection);
        createHomesTable(connection);
        createWarpsTable(connection);
    }
    
    private void createVersionTable(Connection connection) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS aether_schema_version (" +
                     "version INTEGER PRIMARY KEY," +
                     "description TEXT NOT NULL," +
                     "applied_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP" +
                     ")";
        
        executeStatement(connection, sql);
    }
    
    private void createPlayersTable(Connection connection) throws SQLException {
        String idType = databaseType == DatabaseType.MYSQL ? "BINARY(16)" : "TEXT";
        String autoIncrement = databaseType == DatabaseType.MYSQL ? "AUTO_INCREMENT" : "AUTOINCREMENT";
        
        String sql = "CREATE TABLE IF NOT EXISTS aether_players (" +
                     "id INTEGER PRIMARY KEY " + autoIncrement + "," +
                     "uuid " + idType + " NOT NULL UNIQUE," +
                     "username VARCHAR(16) NOT NULL," +
                     "last_login TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                     "first_join TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                     "balance DECIMAL(20, 2) NOT NULL DEFAULT 0," +
                     "last_location TEXT," +
                     "accept_teleport BOOLEAN NOT NULL DEFAULT 1," +
                     "language VARCHAR(10) DEFAULT NULL," +
                     "settings TEXT DEFAULT NULL" +
                     ")";
        
        executeStatement(connection, sql);
    }
    
    private void createHomesTable(Connection connection) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS aether_homes (" +
                     "id INTEGER PRIMARY KEY " + (databaseType == DatabaseType.MYSQL ? "AUTO_INCREMENT" : "AUTOINCREMENT") + "," +
                     "player_id INTEGER NOT NULL," +
                     "name VARCHAR(50) NOT NULL," +
                     "world VARCHAR(50) NOT NULL," +
                     "x DOUBLE NOT NULL," +
                     "y DOUBLE NOT NULL," +
                     "z DOUBLE NOT NULL," +
                     "yaw FLOAT NOT NULL," +
                     "pitch FLOAT NOT NULL," +
                     "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                     "updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                     "UNIQUE(player_id, name)," +
                     "FOREIGN KEY (player_id) REFERENCES aether_players(id) ON DELETE CASCADE" +
                     ")";
        
        executeStatement(connection, sql);
    }
    
    private void createWarpsTable(Connection connection) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS aether_warps (" +
                     "id INTEGER PRIMARY KEY " + (databaseType == DatabaseType.MYSQL ? "AUTO_INCREMENT" : "AUTOINCREMENT") + "," +
                     "name VARCHAR(50) NOT NULL UNIQUE," +
                     "world VARCHAR(50) NOT NULL," +
                     "x DOUBLE NOT NULL," +
                     "y DOUBLE NOT NULL," +
                     "z DOUBLE NOT NULL," +
                     "yaw FLOAT NOT NULL," +
                     "pitch FLOAT NOT NULL," +
                     "description TEXT," +
                     "permission VARCHAR(100)," +
                     "cost DECIMAL(20, 2) DEFAULT 0," +
                     "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                     "created_by VARCHAR(36)," +
                     "updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP" +
                     ")";
        
        executeStatement(connection, sql);
    }
}