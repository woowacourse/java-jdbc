package com.techcourse.support.transaction;

import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.function.ThrowingSupplier;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class TransactionTemplate {

    private static final Logger log = LoggerFactory.getLogger(TransactionTemplate.class);

    private final DataSource dataSource;

    public TransactionTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void execute(Runnable callback) {
        execute(() -> {
            callback.run();
            return null;
        });
    }

    @Nullable
    public <T> T execute(ThrowingSupplier<T> callback) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        try {
            connection.setAutoCommit(false);
            try {
                final var result = callback.get();
                connection.commit();
                return result;
            } catch (Exception e) {
                rollback(e, connection);
                throw e;
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    private void rollback(Exception appException, Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException rollbackEx) {
            log.error(rollbackEx.getMessage(), rollbackEx);
            appException.addSuppressed(rollbackEx);
        }
    }
}
