package org.springframework.transaction.support;

import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.function.Supplier;

public class TransactionHandler {

    private final DataSource dataSource;

    public TransactionHandler(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void handle(Runnable runnable) {
        Connection connection = null;
        try {
            connection = DataSourceUtils.getConnection(dataSource);
            connection.setAutoCommit(false);

            runnable.run();

            connection.commit();
        } catch (Exception e) {
            ConnectionManager.rollback(e, connection);
        } finally {
            ConnectionManager.close(dataSource, connection);
        }
    }

    public <T> T handle(Supplier<T> supplier) {
        Connection connection = null;
        try {
            connection = DataSourceUtils.getConnection(dataSource);
            connection.setAutoCommit(false);

            T result = supplier.get();

            connection.commit();
            return result;
        } catch (Exception e) {
            ConnectionManager.rollback(e, connection);
            return null;
        } finally {
            ConnectionManager.close(dataSource, connection);
        }
    }
}
