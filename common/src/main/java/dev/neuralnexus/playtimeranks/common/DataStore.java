package dev.neuralnexus.playtimeranks.common;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import dev.neuralnexus.playtimeranks.common.config.sections.DatabaseConfig;

import java.sql.Connection;
import java.sql.SQLException;

public class DataStore {
    private static final HikariConfig dbConfig = new HikariConfig();
    private static HikariDataSource ds;

    public DataStore(DatabaseConfig config) {
        dbConfig.setJdbcUrl("jdbc:mysql://" + config.address() + "/" + config.database());
        dbConfig.setUsername(config.username());
        dbConfig.setPassword(config.password());

        dbConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dbConfig.addDataSourceProperty("cachePrepStmts", "true");
        dbConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        dbConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        ds = new HikariDataSource(dbConfig);
    }

    public void close() {
        ds.close();
    }

    public Connection getConnection() throws SQLException {
        return ds.getConnection();
    }
}
