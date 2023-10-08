package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.jdbc.datasource.DataSourceUtils;

public class TransactionManager {
    private final DataSource dataSource;

    public TransactionManager(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void start() {
        process(() -> getConnection().setAutoCommit(false));
    }

    public void commit() {
        process(getConnection()::commit);
    }

    public void rollback() {
        process(getConnection()::rollback);
    }

    public void release() {
        DataSourceUtils.releaseConnection(dataSource);
    }

    private Connection getConnection() {
        return DataSourceUtils.getConnection(dataSource);
    }

    private void process(final TransactionProcessor processor) {
        try {
            processor.process();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
