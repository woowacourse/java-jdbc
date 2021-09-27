package nextstep.jdbc.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcResourceCloser {

    private static final Logger log = LoggerFactory.getLogger(JdbcResourceCloser.class);

    public static void closeConnection(Connection connection) {
        if (connection == null) {
            return;
        }
        try {
            if (TransactionManager.isInTransaction()) {
                return;
            }
            connection.close();
        } catch (SQLException sqlException) {
            log.error(sqlException.getMessage());
        }
    }

    public static void closeStatement(Statement stmt) {
        if (stmt == null) {
            return;
        }
        try {
            stmt.close();
        } catch (SQLException sqlException) {
            log.error(sqlException.getMessage());
        }
    }

    public static void closePreparedStatement(PreparedStatement preparedStatement) {
        if (preparedStatement == null) {
            return;
        }
        try {
            preparedStatement.close();
        } catch (SQLException sqlException) {
            log.error(sqlException.getMessage());
        }
    }
}
