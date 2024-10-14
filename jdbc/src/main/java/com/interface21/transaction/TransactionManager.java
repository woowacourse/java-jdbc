package com.interface21.transaction;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import java.sql.Connection;
import java.util.function.Consumer;
import javax.sql.DataSource;

public class TransactionManager {

    private final DataSource dataSource;

    public TransactionManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void performTransaction(final Consumer<Connection> operation) {
        final Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            begin(connection);
            operation.accept(connection);
            commit(connection);
        } catch (Exception e) {
            rollback(connection);
            throw new DataAccessException("에러가 발생해 트랜잭션이 롤백되었습니다.", e);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    private void begin(final Connection connection) {
        try {
            connection.setAutoCommit(false);
        } catch (Exception e) {
            throw new DataAccessException("트랜잭션 시작에 실패했습니다.", e);
        }
    }

    private void commit(final Connection connection) {
        try {
            connection.commit();
        } catch (Exception e) {
            throw new DataAccessException("트랜잭션 커밋에 실패했습니다.", e);
        }
    }

    private void rollback(final Connection connection) {
        try {
            connection.rollback();
        } catch (Exception e) {
            throw new DataAccessException("트랜잭션 롤백에 실패했습니다.", e);
        }
    }
}
