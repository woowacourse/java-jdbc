package com.techcourse.support.transaction;

import com.interface21.exception.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public class TransactionInterceptor {

    private DataSource datasource;

    public TransactionInterceptor(DataSource datasource) {
        this.datasource = datasource;
    }

    public <T> T applyTransaction(TransactionTargetMethod<T> callback) {
        T result = null;
        Connection connection = null;
        try {
            connection = DataSourceUtils.getConnection(datasource);
            connection.setAutoCommit(false);
            result = callback.call();
            connection.commit();
        } catch (SQLException | DataAccessException e) {
            rollbackTransaction(connection);
            throw new DataAccessException("DB 작업에 실패했습니다.", e);
        } finally {
            closeConnection(connection);
        }
        return result;
    }

    private void rollbackTransaction(Connection connection) {
        if (connection != null) {
            try {
                connection.rollback();
            } catch (SQLException e) {
                throw new DataAccessException("트랜잭션을 롤백할 수 없습니다!", e);
            }
        }
    }

    private void closeConnection(Connection connection) {
        DataSourceUtils.releaseConnection(connection, datasource);
    }
}
