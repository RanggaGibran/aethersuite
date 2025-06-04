package id.rnggagib.aethersuite.core.database.providers;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import id.rnggagib.aethersuite.api.database.ConnectionProvider;
import id.rnggagib.aethersuite.api.database.DatabaseType;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public abstract class HikariConnectionProvider implements ConnectionProvider {
    protected final HikariDataSource dataSource;
    protected final String databaseName;
    protected final DatabaseType type;
    
    protected HikariConnectionProvider(String databaseName, DatabaseType type, HikariConfig config) {
        this.databaseName = databaseName;
        this.type = type;
        this.dataSource = new HikariDataSource(config);
    }
    
    @Override
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
    
    @Override
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
    
    @Override
    public boolean isClosed() {
        return dataSource == null || dataSource.isClosed();
    }
    
    @Override
    public String getDatabaseName() {
        return databaseName;
    }
    
    @Override
    public DatabaseType getType() {
        return type;
    }
}