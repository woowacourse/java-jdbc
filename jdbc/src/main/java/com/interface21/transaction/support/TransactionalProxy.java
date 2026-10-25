package com.interface21.transaction.support;

import com.interface21.dao.DataAccessException;
import com.interface21.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.SQLException;
import java.util.Objects;

public final class TransactionalProxy implements InvocationHandler {

    private final Object target;
    private final DataSource dataSource;

    private TransactionalProxy(final Object target, final DataSource dataSource) {
        this.target = Objects.requireNonNull(target);
        this.dataSource = Objects.requireNonNull(dataSource);
    }

    public static <T> T create(final Class<T> serviceType, final T target, final DataSource dataSource) {
        return serviceType.cast(Proxy.newProxyInstance(serviceType.getClassLoader(),
                new Class<?>[]{serviceType}, new TransactionalProxy(target, dataSource)));
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        final Method implementation = target.getClass().getMethod(method.getName(), method.getParameterTypes());
        if (!method.isAnnotationPresent(Transactional.class)
                && !implementation.isAnnotationPresent(Transactional.class)) {
            return invokeTarget(implementation, args);
        }
        if (TransactionSynchronizationManager.getResource(dataSource) != null) {
            throw new IllegalStateException("Nested transactional proxy calls are not supported");
        }
        try (final var connection = dataSource.getConnection()) {
            final boolean autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            Throwable failure = null;
            boolean canRestore = true;
            try {
                TransactionSynchronizationManager.bindResource(dataSource, connection);
                final Object result = invokeTarget(implementation, args);
                connection.commit();
                return result;
            } catch (Throwable exception) {
                failure = exception;
                try {
                    connection.rollback();
                } catch (SQLException rollbackFailure) {
                    canRestore = false;
                    exception.addSuppressed(rollbackFailure);
                }
                throw exception;
            } finally {
                TransactionSynchronizationManager.unbindResource(dataSource);
                try {
                    if (canRestore) {
                        connection.setAutoCommit(autoCommit);
                    }
                } catch (SQLException restoreFailure) {
                    if (failure != null) {
                        failure.addSuppressed(restoreFailure);
                    } else {
                        throw restoreFailure;
                    }
                }
            }
        } catch (SQLException exception) {
            throw new DataAccessException("Failed to complete transaction", exception);
        }
    }

    private Object invokeTarget(final Method method, final Object[] args) throws Throwable {
        try {
            return method.invoke(target, args);
        } catch (InvocationTargetException exception) {
            throw exception.getCause();
        }
    }
}
