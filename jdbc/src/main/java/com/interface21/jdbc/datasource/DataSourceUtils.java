package com.interface21.jdbc.datasource;

import com.interface21.jdbc.CannotGetJdbcConnectionException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

// 4단계 미션에서 사용할 것
public abstract class DataSourceUtils {

    private static final ThreadLocal<Map<DataSource, Connection>> connectionHolder = new ThreadLocal<>();

    static {
        connectionHolder.set(new HashMap<>());
    }

    private DataSourceUtils() {

    }

    public static Connection getConnection(DataSource dataSource) throws CannotGetJdbcConnectionException {
        Map<DataSource, Connection> dataSourceConnectionMap = connectionHolder.get();

        Connection connection = dataSourceConnectionMap.get(dataSource);
        if (connection != null) {
            return connection;
        }

        try {
            dataSourceConnectionMap.put(dataSource,dataSource.getConnection());
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new CannotGetJdbcConnectionException("Failed to obtain JDBC Connection");
        }
    }

    public static void releaseConnection(DataSource dataSource) {
        try {
            Map<DataSource, Connection> dataSourceConnectionMap = connectionHolder.get();
            Connection connection = dataSourceConnectionMap.get(dataSource);
            if (connection != null) {
                dataSourceConnectionMap.remove(dataSource);
                connection.close();
                return;
            }

            throw new CannotGetJdbcConnectionException("Failed to obtain JDBC Connection");
        } catch (SQLException ex) {
            throw new CannotGetJdbcConnectionException("Failed to close JDBC Connection");
        }
    }
}
