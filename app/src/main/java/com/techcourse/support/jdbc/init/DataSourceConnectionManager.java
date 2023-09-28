package com.techcourse.support.jdbc.init;

import com.techcourse.config.DataSourceConfig;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.ConnectionManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class DataSourceConnectionManager implements ConnectionManager {

    public Connection getConnection() throws CannotGetJdbcConnectionException {
        try {
            DataSource dataSource = DataSourceConfig.getInstance();
            return dataSource.getConnection();
        } catch (final SQLException exception) {
            throw new CannotGetJdbcConnectionException("Datasource connection error:" + exception.getMessage());
        }
    }

}
