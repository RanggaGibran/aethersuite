package id.rnggagib.aethersuite.core.database.migrations;

import id.rnggagib.aethersuite.api.database.Migration;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public abstract class BaseMigration implements Migration {
    private final int version;
    private final String description;
    
    protected BaseMigration(int version, String description) {
        this.version = version;
        this.description = description;
    }
    
    @Override
    public int getVersion() {
        return version;
    }
    
    @Override
    public String getDescription() {
        return description;
    }
    
    protected void executeStatement(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }
    
    protected void executeStatements(Connection connection, String... sqlStatements) throws SQLException {
        for (String sql : sqlStatements) {
            executeStatement(connection, sql);
        }
    }
}