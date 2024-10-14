package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Supplier;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TxUserService implements UserService {

    private static final Logger log = LoggerFactory.getLogger(TxUserService.class);

    private final UserService userService;

    public TxUserService(UserService userService) {
        this.userService = userService;
    }

    private <T> T executeInTransaction(Supplier<T> service) {
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

    private void rollbackTransaction(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException e) {
            log.info("ROLLBACK_TRANSACTION_ERROR :: {}", e.getMessage(), e);
            throw new DataAccessException("트랜잭션을 롤백하던 중 예외가 발생했습니다.");
        }
    }

    @Override
    public User findById(long id) {
        return executeInTransaction(() -> userService.findById(id));
    }

    @Override
    public void insert(User user) {
        executeInTransaction(() -> {
            userService.insert(user);
            return null;
        });
    }

    @Override
    public void changePassword(long id, String newPassword, String createBy) {
        executeInTransaction(() -> {
            userService.changePassword(id, newPassword, createBy);
            return null;
        });
    }
}
