package com.interface21.jdbc.datasource;

import com.interface21.transaction.support.TransactionSynchronizationManager;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomDataSourceUtils {

    private static final Logger log = LoggerFactory.getLogger(CustomDataSourceUtils.class);

    private CustomDataSourceUtils() {
    }

    public static Connection getConnection(DataSource dataSource) throws SQLException {
        Connection connection = TransactionSynchronizationManager.getResource(dataSource);

        if (connection != null) {
            return connection;
        }

        connection = dataSource.getConnection();
        return connection;
    }

    public static void releaseConnection(Connection connection, DataSource dataSource) {
        if (connection == null) {
            return;
        }

        try {
            Connection transactionConnection = TransactionSynchronizationManager.getResource(dataSource);

            if (connection == transactionConnection) {
                return;
            }

            connection.close();
        } catch (SQLException e) {
            log.error("Failed to close connection");
        }
    }
}
