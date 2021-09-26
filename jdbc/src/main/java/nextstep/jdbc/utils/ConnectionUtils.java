package nextstep.jdbc.utils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;

public class ConnectionUtils {

    private static final Map<String, ConnectionInfo> CONNECTIONS = new HashMap<>();

    private static class ConnectionInfo {

        private Connection connection;
        private boolean transactionStarted;

        public Connection getConnection(DataSource dataSource) throws SQLException {
            if (connection == null) {
                connection = dataSource.getConnection();
            }

            if (transactionStarted) {
                connection.setAutoCommit(false);
            }
            return connection;
        }

        public Connection getConnection() {
            return connection;
        }

        public void closeConnection() throws SQLException {
            if (connection != null) {
                connection.close();
            }
        }

        public void startTransaction() {
            transactionStarted = true;
        }
    }

    public static void startTransaction() {
        CONNECTIONS
            .computeIfAbsent(currentThread(), name -> new ConnectionInfo())
            .startTransaction();
    }

    public static void endTransaction() {
        try {
            closeConnection();
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
    }

    public static boolean isTransactionStarted() {
        return CONNECTIONS
            .computeIfAbsent(currentThread(), name -> new ConnectionInfo())
            .transactionStarted;
    }

    public static Connection getConnection(DataSource dataSource) throws SQLException {
        return CONNECTIONS
            .computeIfAbsent(currentThread(), name -> new ConnectionInfo())
            .getConnection(dataSource);
    }

    public static Connection getConnection() throws SQLException {
        return CONNECTIONS
            .computeIfAbsent(currentThread(), name -> new ConnectionInfo())
            .getConnection();
    }

    public static void closeConnection() throws SQLException {
        final String currentThread = currentThread();
        if (CONNECTIONS.containsKey(currentThread)) {
            CONNECTIONS.get(currentThread).closeConnection();
            CONNECTIONS.remove(currentThread);
        }
    }

    private static String currentThread() {
        return Thread.currentThread().getName();
    }
}
