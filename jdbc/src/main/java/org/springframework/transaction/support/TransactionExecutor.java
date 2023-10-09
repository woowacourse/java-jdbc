package org.springframework.transaction.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Supplier;

public class TransactionExecutor {

    private static final Logger log = LoggerFactory.getLogger(TransactionExecutor.class);

    private final DataSource dataSource;
    private final Connection connection;

    public TransactionExecutor(final DataSource dataSource) {
        this.dataSource = dataSource;
        this.connection = DataSourceUtils.getConnection(dataSource);
    }

    public void execute(Runnable action) {
        try {
            try {
                connection.setAutoCommit(false);
                log.info("transaction start");

                action.run();

                connection.commit();
                log.info("transaction commit");
            } catch (Exception e) {
                connection.rollback();
                log.info("transaction rollback");

                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    public <T> T execute(Supplier<T> action) {
        try {
            try {
                connection.setAutoCommit(false);
                log.info("transaction start");

                final T returnValue = action.get();

                connection.commit();
                log.info("transaction commit");

                return returnValue;
            } catch (Exception e) {
                connection.rollback();
                log.info("transaction rollback");

                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }
}
