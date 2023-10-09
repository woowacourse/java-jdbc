package org.springframework.transaction.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Consumer;

public class TransactionExecutor {

    private static final Logger log = LoggerFactory.getLogger(TransactionExecutor.class);

    private final DataSource dataSource;

    public TransactionExecutor(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void execute(Consumer<Connection> action) {
        try (final Connection connection = dataSource.getConnection()) {
            try {
                connection.setAutoCommit(false);
                log.info("transaction start");

                action.accept(connection);

                connection.commit();
                log.info("transaction commit");
            } catch (Exception e) {
                connection.rollback();
                log.info("transaction rollback");

                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
