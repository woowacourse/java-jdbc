package com.interface21.transaction.support;

import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.Transactional;
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

    public TransactionHandler(DataSource dataSource, Object target) {
        this.dataSource = dataSource;
        this.target = target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 기존 구현체 Method를 호출하게 되면, 리플렉션이 2배가 되어 성능 저하 발생
        // 따라서 인터페이스에 애노테이션이 붙어있는지 확인
        if (!method.isAnnotationPresent(Transactional.class)) {
            return method.invoke(target, args);
        }

        Connection existingConnection = TransactionSynchronizationManager.getResource(dataSource);
        boolean isNewTransaction = (existingConnection == null);

        Connection connection = DataSourceUtils.getConnection(dataSource);
        boolean originalAutoCommit = connection.getAutoCommit();

        try {
            if (isNewTransaction && originalAutoCommit) {
                connection.setAutoCommit(false);
            }
            return getObject(args, method, connection);
        } finally {
            finalizeTransaction(isNewTransaction, originalAutoCommit, connection);
        }
    }

    private void finalizeTransaction(boolean isNewTransaction, boolean originalAutoCommit, Connection connection) {
        if (isNewTransaction) {
            revertAutoCommit(originalAutoCommit, connection);
            TransactionSynchronizationManager.unbindResource(dataSource);
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    private void revertAutoCommit(boolean originalAutoCommit, Connection connection) {
        try {
            if (originalAutoCommit) {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            log.error("Could not reset JDBC Connection after transaction", e);
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
        }catch (SQLException e){
            rollback(connection, e);
            throw e;
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
