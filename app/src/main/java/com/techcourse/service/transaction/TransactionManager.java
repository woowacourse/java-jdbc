package com.techcourse.service.transaction;

import com.interface21.jdbc.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

public class TransactionManager {

    private final DataSource dataSource;

    public TransactionManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void transaction(TransactionConsumer consumer) {
        executeTransaction(connection -> {
            consumer.execute(connection);
            return Optional.empty();
        });
    }

    public <T> T transaction(TransactionFunction<T> function) {
        return executeTransaction(function);
    }

    private <T> T executeTransaction(TransactionFunction<T> function) {
        Connection connection = null;

        try {
            connection = DataSourceUtils.getConnection(dataSource);
            connection.setAutoCommit(false);

            T result = function.apply(connection);

            connection.commit();
            return result;
        } catch (SQLException exception) {
            rollback(connection);
            throw new DataAccessException("트랜잭션이 실패했습니다.", exception);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    private void rollback(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException exception) {
            throw new DataAccessException("롤백 중 예외가 발생했습니다.", exception);
        }
    }
}
