package com.interface21.transaction.aop;

import com.interface21.dao.DataAccessException;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

public class TransactionAdvice implements MethodInterceptor {

    private final DataSource dataSource;

    public TransactionAdvice(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Object invoke(final MethodInvocation invocation) throws Throwable {
        try (final Connection conn = dataSource.getConnection()) {
            TransactionSynchronizationManager.bindResource(dataSource, conn);
            try {
                conn.setAutoCommit(false);
                final Object result = invocation.proceed();
                conn.commit();
                return result;
            } catch (RuntimeException | Error e) {
                conn.rollback();
                throw e;
            } finally {
                TransactionSynchronizationManager.unbindResource(dataSource);
            }
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }
}
