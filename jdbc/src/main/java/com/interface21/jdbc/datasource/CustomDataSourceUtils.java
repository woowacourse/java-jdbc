package com.interface21.jdbc.datasource;

import com.interface21.transaction.support.TransactionSynchronizationManager;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public class CustomDataSourceUtils {

    private CustomDataSourceUtils() {
    }

    public static Connection getConnection(DataSource dataSource) throws SQLException {
        Connection connection = TransactionSynchronizationManager.getResource(dataSource);

        if (connection != null) {
            return connection;
        }

        connection = dataSource.getConnection();
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        return connection;
    }
}
