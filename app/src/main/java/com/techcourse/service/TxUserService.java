package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import java.sql.Connection;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TxUserService implements UserService {

    private static final Logger log = LoggerFactory.getLogger(TxUserService.class);

    private final UserService userService;

    public TxUserService(UserService userService) {
        this.userService = userService;
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
        Connection connection = DataSourceUtils.getConnection(DataSourceConfig.getInstance());
        try (connection) {
            connection.setAutoCommit(false);

            User user = userService.findById(id);

            connection.commit();
            return user;
        } catch (SQLException e) {
            rollbackTransaction(connection);
            log.info("FIND_BY_ID_TRANSACTION_ERROR :: {}", e.getMessage(), e);
            throw new DataAccessException("사용자를 조회하던 중 오류가 발생했습니다.");
        } finally {
            TransactionSynchronizationManager.unbindResource(DataSourceConfig.getInstance());
        }
    }

    @Override
    public void insert(User user) {
        Connection connection = DataSourceUtils.getConnection(DataSourceConfig.getInstance());
        try (connection) {
            connection.setAutoCommit(false);

            userService.insert(user);

            connection.commit();
        } catch (SQLException e) {
            rollbackTransaction(connection);
            log.info("INSERT_TRANSACTION_ERROR :: {}", e.getMessage(), e);
            throw new DataAccessException("사용자를 생서하던 중 오류가 발생했습니다.");
        } finally {
            TransactionSynchronizationManager.unbindResource(DataSourceConfig.getInstance());
        }
    }

    @Override
    public void changePassword(long id, String newPassword, String createBy) {
        Connection connection = DataSourceUtils.getConnection(DataSourceConfig.getInstance());
        try (connection) {
            connection.setAutoCommit(false);

            userService.changePassword(id, newPassword, createBy);

            connection.commit();
        } catch (SQLException e) {
            rollbackTransaction(connection);
            log.info("CHANGE_PASSWORD_TRANSACTION_ERROR :: {}", e.getMessage(), e);
            throw new DataAccessException("비밀번호를 변경하던 중 오류가 발생했습니다.");
        } finally {
            TransactionSynchronizationManager.unbindResource(DataSourceConfig.getInstance());
        }
    }
}
