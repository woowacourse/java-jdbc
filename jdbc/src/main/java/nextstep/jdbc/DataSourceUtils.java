package nextstep.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import nextstep.transaction.TransactionSynchronizationManager;

public class DataSourceUtils {

    public static Connection getConnection(final DataSource dataSource) throws SQLException {
        Object resource = TransactionSynchronizationManager.getResource(dataSource);
        if (resource == null) {
            Connection connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            TransactionSynchronizationManager.bindConnection(dataSource, connection);
            return connection;
        }
        return (Connection) resource;
    }

    public static void release(final DataSource dataSource) throws SQLException {
        Connection releasedConnection = (Connection) TransactionSynchronizationManager.release(dataSource);
        if (releasedConnection != null && !releasedConnection.isClosed()) {
            releasedConnection.close();
        }
    }

    private DataSourceUtils() {
    }
}
