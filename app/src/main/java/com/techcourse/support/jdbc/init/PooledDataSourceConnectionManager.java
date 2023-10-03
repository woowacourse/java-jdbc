package com.techcourse.support.jdbc.init;

import com.techcourse.config.DataSourceConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.ConnectionManager;

public class PooledDataSourceConnectionManager implements ConnectionManager {

    private final HikariDataSource dataSource;

    public PooledDataSourceConnectionManager() {
        this.dataSource = (HikariDataSource) DataSourceConfig.getInstance();
    }

    @Override
    public Connection getConnection() throws CannotGetJdbcConnectionException {
        try {
            final var connection = dataSource.getConnection();
            return connection;
        } catch (final SQLException exception) {
            throw new CannotGetJdbcConnectionException("Datasource connection error:" + exception.getMessage());
        }
    }

}
