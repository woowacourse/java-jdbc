package com.interface21.jdbc.core;

import java.sql.Connection;
import javax.sql.DataSource;

public class TransactionTemplate {

    private final DataSource dataSource;

    public TransactionTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T execute(TransactionCallBack<T> transactionCallBack) throws Exception {
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            T result = transactionCallBack.execute(connection);
            connection.commit();
            return result;
        } catch (Exception e) {
            if (connection != null) {
                connection.rollback();
            }
            throw e;
        }
    }
}
