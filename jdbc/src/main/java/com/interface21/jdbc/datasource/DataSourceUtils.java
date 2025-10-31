package com.interface21.jdbc.datasource;

import com.interface21.transaction.support.TransactionSynchronizationManager;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public abstract class DataSourceUtils {

    private DataSourceUtils() {
    }

    public static Connection getConnection(DataSource dataSource) throws SQLException {
        Connection conn = (Connection) TransactionSynchronizationManager.getResource(dataSource);
        if (conn != null) {
            return conn;
        }

        return dataSource.getConnection();
    }

    public static void releaseConnection(Connection conn, DataSource dataSource) throws SQLException {
        if (TransactionSynchronizationManager.hasResource(dataSource)) {
            return;
        }

        if (conn != null) {
            conn.close();
        }
    }
}
