package com.interface21.transaction.support;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.transaction.Transactional;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public class TransactionHandler implements InvocationHandler {

    private final DataSource dataSource;
    private final Object target;

    public TransactionHandler(DataSource dataSource, Object target) {
        this.dataSource = dataSource;
        this.target = target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Method targetMethod = target.getClass().getMethod(method.getName(), method.getParameterTypes());

        if (!targetMethod.isAnnotationPresent(Transactional.class)) {
            return targetMethod.invoke(target, args);
        }

        // 트랜잭션 시작
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            JdbcTemplate.setCurrentConnection(connection);
            return getObject(args, targetMethod, connection);
        }
    }

    private Object getObject(Object[] args, Method targetMethod, Connection connection) throws Throwable {
        try {
            Object result = targetMethod.invoke(target, args);
            connection.commit();
            return result;
        } catch (InvocationTargetException e) {
            Throwable cause = e.getTargetException();
            rollback(connection, cause);
            throw cause;
        } finally {
            JdbcTemplate.clearCurrentConnection();
        }
    }

    private void rollback(Connection connection, Throwable cause) {
        try {
            connection.rollback();
        } catch (SQLException sqlE) {
            cause.addSuppressed(sqlE);
        }
    }
}
