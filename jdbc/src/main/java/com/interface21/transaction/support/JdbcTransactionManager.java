package com.interface21.transaction.support;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import com.interface21.jdbc.support.h2.H2SQLExceptionTranslator;

public class JdbcTransactionManager {

    private final DataSource dataSource;
    private final H2SQLExceptionTranslator exceptionTranslator;

    public JdbcTransactionManager(DataSource dataSource) {
        this.dataSource = dataSource;
        this.exceptionTranslator = new H2SQLExceptionTranslator();
    }

    public JdbcTransaction getTransaction() {
        try {
            Connection connection = dataSource.getConnection();
            return new JdbcTransaction(connection);
        } catch (SQLException e) {
            throw exceptionTranslator.translate(e);
        }
    }
}
