package com.techcourse.service;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.dao.SQLTransactionRollbackException;
import org.springframework.jdbc.datasource.DataSourceUtils;

public class TransactionProxyHandler implements InvocationHandler {

    private final Object target;
    private final DataSource dataSource;

    public TransactionProxyHandler(final Object target, final DataSource dataSource) {
        this.target = target;
        this.dataSource = dataSource;
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        final Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            connection.setAutoCommit(false);
            final Object result = method.invoke(target, args);
            connection.commit();

            return result;
        } catch (RuntimeException | SQLException e) {
            throw handleTransactionFailure(connection, e);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    private RuntimeException handleTransactionFailure(final Connection connection, final Exception e) {
        try {
            connection.rollback();
            return new RuntimeException(e);
        } catch (SQLException ex) {
            return new SQLTransactionRollbackException(ex);
        }
    }
}
