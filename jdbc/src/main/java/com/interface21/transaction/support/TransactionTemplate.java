package com.interface21.transaction.support;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Consumer;
import javax.sql.DataSource;

public class TransactionTemplate {

    private final DataSource dataSource;

    public TransactionTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void execute(final Consumer<Connection> callback) {
        try (Connection connection = dataSource.getConnection()) {
            final boolean autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            try {
                callback.accept(connection);
                connection.commit();
            } catch (SQLException | RuntimeException | Error e) {
                rollbackAndRestoreAutoCommit(connection, autoCommit, e);
                throw e;
            }

            connection.setAutoCommit(autoCommit);
        } catch (SQLException e) {
            throw new DataAccessException("트랜잭션 처리에 실패했습니다.", e);
        }
    }

    private void rollbackAndRestoreAutoCommit(
            final Connection connection,
            final boolean autoCommit,
            final Throwable failure
    ) {
        try {
            connection.rollback();
            connection.setAutoCommit(autoCommit);
        } catch (SQLException e) {
            failure.addSuppressed(e);
        }
    }
}
