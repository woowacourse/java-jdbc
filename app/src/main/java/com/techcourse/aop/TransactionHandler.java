package com.techcourse.aop;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.aop.annotation.Transactional;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionHandler implements InvocationHandler {

    private static final Logger log = LoggerFactory.getLogger(TransactionHandler.class);

    private final DataSource dataSource;
    private final Object target;

    public TransactionHandler(final DataSource dataSource, final Object target) {
        this.dataSource = dataSource;
        this.target = target;
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        final Method targetMethod = target.getClass().getMethod(method.getName(), method.getParameterTypes());
        final Transactional transactional = targetMethod.getAnnotation(Transactional.class);

        if (transactional == null) {
            return invokeWithoutTransaction(method, args);
        }

        return invokeWithTransaction(method, args);
    }

    private Object invokeWithoutTransaction(final Method method, final Object[] args) throws Throwable {
        try {
            return method.invoke(target, args);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }

    private Object invokeWithTransaction(final Method method, final Object[] args) throws Throwable {
        Connection conn = null;
        boolean originalAutoCommit = true;

        try {
            conn = DataSourceUtils.getConnection(dataSource);
            TransactionSynchronizationManager.bindResource(dataSource, conn);
            originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            final Object result = method.invoke(target, args);

            conn.commit();
            return result;
        } catch (InvocationTargetException e) {
            throw handleException(conn, e);
        } finally {
            cleanUpConnection(conn, originalAutoCommit);
        }
    }

    private Throwable handleException(final Connection conn, final InvocationTargetException e) {
        Throwable targetException = e.getTargetException();
        log.error(targetException.getMessage(), targetException);

        if (targetException instanceof SQLException) {
            rollback(conn, e);
            return new DataAccessException(targetException.getMessage(), targetException);
        }

        if (targetException instanceof RuntimeException || targetException instanceof Error) {
            rollback(conn, e);
            return targetException;
        }

        commit(conn, e);
        return targetException;
    }

    private void rollback(Connection conn, Exception originalException) {
        try {
            conn.rollback();
        } catch (SQLException rollbackEx) {
            log.error("Rollback failed", rollbackEx);
            originalException.addSuppressed(rollbackEx);
        }
    }

    private void commit(Connection conn, Exception originalException) {
        try {
            conn.commit();
        } catch (SQLException commitEx) {
            log.error("Commit failed", commitEx);
            originalException.addSuppressed(commitEx);
        }
    }

    private void cleanUpConnection(final Connection conn, final boolean originalAutoCommit) {
        if (conn == null) {
            return;
        }

        TransactionSynchronizationManager.unbindResource(dataSource);
        restoreAutoCommit(conn, originalAutoCommit);
        closeConnection(conn);
    }

    private void restoreAutoCommit(final Connection conn, final boolean originalAutoCommit) {
        try {
            conn.setAutoCommit(originalAutoCommit);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }
    }

    private void closeConnection(final Connection conn) {
        try {
            conn.close();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }
    }
}
