package com.techcourse.support;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.config.DataSourceConfig;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Supplier;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionExecutor {

    private static final Logger log = LoggerFactory.getLogger(TransactionExecutor.class);

    public static <T> T executeInTransaction(Supplier<T> service) {
        DataSource dataSource = DataSourceConfig.getInstance();
        Connection connection = DataSourceUtils.getConnection(dataSource);

        try (connection) {
            connection.setAutoCommit(false);

            T data = service.get();

            connection.commit();
            return data;
        } catch (Exception e) {
            rollbackTransaction(connection);
            log.info("RUN_TRANSACTION_ERROR :: {}", e.getMessage(), e);
            throw new DataAccessException("트랜잭션 실행 중 예외가 발생했습니다.");
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }

    private static void rollbackTransaction(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException e) {
            log.info("ROLLBACK_TRANSACTION_ERROR :: {}", e.getMessage(), e);
            throw new DataAccessException("트랜잭션을 롤백하던 중 예외가 발생했습니다.");
        }
    }
}
