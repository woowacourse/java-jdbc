package org.springframework.transaction.support;

import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;

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
}
