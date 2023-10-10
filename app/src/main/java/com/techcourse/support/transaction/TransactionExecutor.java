package com.techcourse.support.transaction;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.exception.RollbackFailException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Supplier;

public class TransactionExecutor {

    private final DataSource dataSource;

    public TransactionExecutor(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void execute(final Runnable serviceLogicExecutor) {
        final Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            connection.setAutoCommit(false);
            serviceLogicExecutor.run();
            connection.commit();
        } catch (SQLException | RuntimeException e) {
            rollback(connection);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    public <T> T readExecute(final Supplier<T> serviceLogicExecutor) {
        T result = null;
        final Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            connection.setReadOnly(true);
            result = serviceLogicExecutor.get();
        } catch (SQLException | RuntimeException e) {
            throw new DataAccessException("데이터에 접근할 수 없습니다.");
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
        return result;
    }

    private static void rollback(final Connection connection) {
        try {
            connection.rollback();
            throw new DataAccessException("데이터에 접근할 수 없습니다.");
        } catch (SQLException exception) {
            throw new RollbackFailException("롤백을 실패했습니다.");
        }
    }
}
