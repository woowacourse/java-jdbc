package com.techcourse.support.transaction;

import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.exception.DataAccessException;
import org.springframework.jdbc.exception.RollbackFailException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class TransactionExecutor {

    private final DataSource dataSource;

    public TransactionExecutor(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void execute(final ServiceLogicExecutor serviceLogicExecutor) {
        final Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            connection.setAutoCommit(false);
            serviceLogicExecutor.execute();
            connection.commit();
        } catch (SQLException e) {
            rollback(connection);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    private static void rollback(Connection connection) {
        try {
            connection.rollback();
            throw new DataAccessException("데이터에 접근할 수 없습니다.");
        } catch (SQLException exception) {
            throw new RollbackFailException("롤백을 실패했습니다.");
        }
    }
}
