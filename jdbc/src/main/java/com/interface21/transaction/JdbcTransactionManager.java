package com.interface21.transaction;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcOperations;
import com.interface21.jdbc.core.JdbcTemplate;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public class JdbcTransactionManager implements TransactionManager {

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    public JdbcTransactionManager(DataSource dataSource) {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public void execute(TransactionWork work) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);

            try {
                JdbcOperations jdbc = (sql, params) -> jdbcTemplate.update(connection, sql, params);
                work.execute(jdbc);
                connection.commit();
            } catch (SQLException | RuntimeException | Error exception) {
                rollback(connection, exception);
                throw exception;
            }
        } catch (SQLException exception) {
            throw new DataAccessException("Failed to execute transaction", exception);
        }
    }

    private void rollback(Connection connection, Throwable originalException) {
        try {
            connection.rollback();
        } catch (SQLException rollbackException) {
            originalException.addSuppressed(rollbackException);
        }
    }
}
