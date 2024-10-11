package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.SQLException;

public class ConnectionUtil {
    public static void setAutoCommitAndClose(Connection connection, boolean autoCommit) {
        if (connection != null) {
            try {
                connection.setAutoCommit(autoCommit);
                connection.close();
            } catch (SQLException e) {
                throw new DataAccessException(e);
            }
        }
    }

    public static void rollback(Connection connection) {
        if (connection != null) {
            try {
                connection.rollback();
            } catch (SQLException e) {
                throw new DataAccessException(e);
            }
        }
    }
}
