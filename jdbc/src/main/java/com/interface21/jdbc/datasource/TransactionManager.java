package com.interface21.jdbc.datasource;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionManager {
    private static final Logger logger = LoggerFactory.getLogger(TransactionManager.class);

    private final ThreadLocal<AtomicLong> transactionDepth = new ThreadLocal<>();
    private final DataSource dataSource;

    public TransactionManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T transaction(Supplier<T> supplier) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        getTransactionDepth().incrementAndGet();
        try {
            beginTransaction(connection);
            T result = supplier.get();
            endTransaction(connection);
            return result;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            rollback(connection);
            throw new DataAccessException(e.getMessage(), e);
        } finally {
            releaseConnection(connection);
        }
    }

    public void transaction(Runnable runnable) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        getTransactionDepth().incrementAndGet();
        try {
            beginTransaction(connection);
            runnable.run();
            endTransaction(connection);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            rollback(connection);
            throw new DataAccessException(e.getMessage(), e);
        } finally {
            releaseConnection(connection);
        }
    }

    private AtomicLong getTransactionDepth() {
        if (transactionDepth.get() == null) {
            transactionDepth.set(new AtomicLong(0));
        }
        return transactionDepth.get();
    }

    private void releaseConnection(Connection connection) {
        if (getTransactionDepth().get() == 1) {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
        getTransactionDepth().decrementAndGet();
    }

    private void beginTransaction(Connection connection) throws SQLException {
        logger.info("begin transaction : {}", connection);
        connection.setAutoCommit(false);
    }

    private void endTransaction(Connection connection) throws SQLException {
        if (getTransactionDepth().get() == 1) {
            connection.commit();
            connection.setAutoCommit(true);
            logger.info("commit transaction : {}", connection);
        }
    }

    private void rollback(Connection connection) {
        try {
            connection.rollback();
            connection.setAutoCommit(true);
            logger.info("rollback transaction : {}", connection);
        } catch (SQLException sqlException) {
            throw new DataAccessException(sqlException);
        }
    }
}
