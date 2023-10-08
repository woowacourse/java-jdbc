package org.springframework.jdbc.datasource;

import org.springframework.jdbc.CannotGetJdbcConnectionException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class ConnectionUtils {

    public static Connection getConnection(final DataSource dataSource) {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new CannotGetJdbcConnectionException("Failed to obtain JDBC Connection", e);
        }
    }

    public static void releaseConnection(final Connection conn) {
        try{
            conn.close();
        } catch (SQLException e) {
            throw new CannotGetJdbcConnectionException("Failed to close JDBC Connection");
        }
    }
}
