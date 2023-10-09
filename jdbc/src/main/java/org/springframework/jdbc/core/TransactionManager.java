package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.jdbc.datasource.DataSourceUtils;

public class TransactionManager implements AutoCloseable {
    private final DataSource dataSource;

    public TransactionManager(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void start() {
        process(connection -> connection.setAutoCommit(false));
    }

    public void commit() {
        process(Connection::commit);
    }

    public void rollback() {
        process(Connection::rollback);
    }

    @Override
    public void close() {
        DataSourceUtils.releaseConnection(dataSource);
    }

    private void process(final TransactionProcessor processor) {
        try {
            final Connection connection = DataSourceUtils.getConnection(dataSource);
            processor.process(connection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
