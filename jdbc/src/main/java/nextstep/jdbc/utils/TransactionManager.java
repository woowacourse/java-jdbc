package nextstep.jdbc.utils;

import java.sql.Connection;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionManager {

    private static final Logger log = LoggerFactory.getLogger(TransactionManager.class);
    private static final ThreadLocal<Connection> connection;
    private static final ThreadLocal<Boolean> transactionSync;

    static {
        connection = new ThreadLocal<>();
        transactionSync = new ThreadLocal<>();
        transactionSync.set(false);
    }

    public static void startTransaction() {
        transactionSync.set(true);
    }

    public static boolean isInTransaction() {
        return transactionSync.get();
    }

    public static void rollback() {
        connectionResult(Connection::rollback);
    }

    public static void commit() {
        connectionResult(Connection::commit);
    }

    public static Connection getConnection() {
        return connection.get();
    }

    public static void setConnection(Connection con) {
        connection.set(con);
    }

    private static void connectionResult(SQLExceptionHandle sqlExceptionHandle) {
        Connection con = connection.get();
        try {
            if(con == null) return;
            sqlExceptionHandle.accept(con);
        } catch (SQLException sqlException) {
            log.error(sqlException.getMessage());
        } finally {
            transactionSync.set(false);
            connection.set(null);
            JdbcResourceCloser.closeConnection(con);
        }
    }

    private interface SQLExceptionHandle {
        void accept(Connection connection) throws SQLException;
    }
}
