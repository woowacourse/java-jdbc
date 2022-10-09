package nextstep.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import nextstep.transaction.TransactionSynchronizationManager;

public class DataSourceUtils {

    public static Connection getConnection(final DataSource dataSource) {
        Object resource = TransactionSynchronizationManager.getResource(dataSource);
        try {
            if (resource == null) {
                Connection connection = dataSource.getConnection();
                TransactionSynchronizationManager.bindConnection(dataSource, connection);
                return connection;
            }
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
        return (Connection) resource;
    }

    public static void release(final Connection connection, final DataSource dataSource) throws SQLException {
        if (!connection.getAutoCommit()) {
            return;
        }
        Connection releasedConnection = (Connection) TransactionSynchronizationManager.release(dataSource);
        if (releasedConnection != null && !releasedConnection.isClosed()) {
            releasedConnection.close();
        }
    }

    private DataSourceUtils() {
    }
}
