package org.springframework.jdbc.support;

import java.sql.SQLException;
import java.sql.Statement;
import org.springframework.jdbc.CannotCloseStatementException;

public abstract class JdbcUtils {

    public static void closeStatement(final Statement statement) {
        try {
            if (statement != null) {
                statement.close();
            }
        } catch (SQLException ignored) {
            throw new CannotCloseStatementException("Failed to close Statement");
        }
    }
}
