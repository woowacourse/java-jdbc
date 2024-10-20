package com.interface21.jdbc.core;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;

public final class TransactionManager {

    private static final Logger log = LoggerFactory.getLogger(TransactionManager.class);

    private final DataSource dataSource;

    public TransactionManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T transactionBegin(LogicExecutor<T> businessLogic) {
        try (Connection connection = DataSourceUtils.getConnection(dataSource)) {
            connection.setAutoCommit(false);
            return execute(connection, businessLogic);
        } catch (SQLException e) {
            log.error("트랜잭션 작업을 시작하기 전 예외가 발생하였습니다: {}", e.getMessage(), e);
            throw new DataAccessException("트랜잭션 작업 전 예외 발생", e);
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }

    private <T> T execute(Connection connection, LogicExecutor<T> businessLogic) throws SQLException {
        try {
            T result = businessLogic.apply(connection);
            connection.commit();
            return result;
        } catch (SQLException e) {
            connection.rollback();
            log.error("트랜잭션 작업 중 예외 발생으로 인한 롤백: {}", e.getMessage(), e);
            throw new DataAccessException("데이터베이스 작업 중 예외 발생", e);
        }
    }
}
