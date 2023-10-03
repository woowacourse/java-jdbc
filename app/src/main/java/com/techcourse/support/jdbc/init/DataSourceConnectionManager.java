package com.techcourse.support.jdbc.init;

import com.techcourse.config.DataSourceConfig;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.ConnectionManager;

public class DataSourceConnectionManager implements ConnectionManager {

    private final DataSource dataSource;

    public DataSourceConnectionManager() {
        this.dataSource = DataSourceConfig.getInstance();
    }

    @Override
    public Connection getConnection() throws CannotGetJdbcConnectionException {
        try {
            return dataSource.getConnection();
        } catch (final SQLException exception) {
            throw new CannotGetJdbcConnectionException("Datasource connection error:" + exception.getMessage());
        }
    }

}
