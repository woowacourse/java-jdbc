package nextstep.jdbc.utils;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public class ConnectionManager {

    public static Connection getConnection(DataSource dataSource) throws SQLException {
        if (TransactionManager.isInTransaction()) {
            return getConnectionInTransaction(dataSource);
        }
        return dataSource.getConnection();
    }

    private static Connection getConnectionInTransaction(DataSource dataSource)
        throws SQLException {
        Connection connection = TransactionManager.getConnection();
        if (connection == null) {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            TransactionManager.setConnection(connection);
        }
        return connection;
    }

    public static void errorHandle() {
        TransactionManager.rollback();
    }
}
