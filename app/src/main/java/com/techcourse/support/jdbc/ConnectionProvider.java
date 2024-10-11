package com.techcourse.support.jdbc;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.Connection;
import com.interface21.jdbc.datasource.ConnectionContext;
import com.techcourse.config.DataSourceConfig;
import java.sql.SQLException;
import javax.sql.DataSource;

public class ConnectionProvider {

    public static Connection getConnection() {
        try {
            Connection conn = ConnectionContext.conn.get();
            if(conn.isClosed()) {
                ConnectionContext.conn.remove();
                throw new NullPointerException();
            }
            return conn;
        } catch (NullPointerException nullPointerException) {
            return createNewConnection();
        }
    }

    private static Connection createNewConnection() {
        try {
            DataSource dataSource = DataSourceConfig.getInstance();
            Connection conn = new Connection(dataSource.getConnection());
            ConnectionContext.conn.set(conn);
            return conn;
        } catch (SQLException sqlException) {
            throw new DataAccessException(sqlException);
        }
    }
}
