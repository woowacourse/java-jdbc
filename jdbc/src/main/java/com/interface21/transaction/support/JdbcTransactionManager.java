package com.interface21.transaction.support;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class JdbcTransactionManager {

    private final DataSource dataSource;

    public JdbcTransactionManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public JdbcTransaction getTransaction() {
        try {
            Connection connection = dataSource.getConnection();
            return new JdbcTransaction(connection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
