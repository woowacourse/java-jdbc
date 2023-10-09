package org.springframework.jdbc.datasource;

import org.springframework.dao.DataAccessException;

import java.sql.Connection;
import java.sql.SQLException;

public class ConnectionHolder {

    private final Connection connection;
    private int referenceCount;

    public ConnectionHolder(Connection connection) {
        this.connection = connection;
        this.referenceCount = 1;
    }

    public Connection getConnection() {
        referenceCount++;
        return connection;
    }

    public boolean close() {
        referenceCount--;
        if (referenceCount == 0) {
            try {
                connection.close();
                return true;
            } catch (SQLException e) {
                throw new DataAccessException(e);
            }
        }
        return false;
    }
}
