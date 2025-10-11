package com.techcourse.service;

import com.interface21.transaction.TransactionException;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.function.ThrowingConsumer;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final DataSource dataSource;
    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;

    public UserService(final DataSource dataSource, final UserDao userDao, final UserHistoryDao userHistoryDao) {
        this.dataSource = dataSource;
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
    }

    public User findById(final long id) {
        return userDao.findById(id);
    }

    public void insert(final User user) {
        userDao.insert(user);
    }

    public void changePassword(final long id, final String newPassword, final String createdBy) {
        executeInTransaction(connection -> {
            final var user = userDao.findById(connection, id);
            user.changePassword(newPassword);

            userDao.update(connection, user);
            userHistoryDao.log(connection, new UserHistory(user, createdBy));
        });
    }

    // TODO. 4단계 - Transaction synchronization 적용하기
    private void executeInTransaction(final ThrowingConsumer<Connection> action) {
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);

            action.acceptWithException(connection);

            connection.commit();
        } catch (final RuntimeException | Error ex) {
            rollbackSafely(connection);
            throw ex;
        } catch (final SQLException e) {
            rollbackSafely(connection);
            throw new TransactionException("SQL error during transaction", e);
        } catch (final Exception e) {
            commitSafely(connection);
            // NOTE. Checked Exception은 스프링처럼 그대로 전파하는 것이 이상적이지만,
            // 리플렉션 기반 호출이 아닌 이상 일반 메서드에서는 throws Exception 시그니처가 필요합니다.
            // 여기서는 트랜잭션 경계를 단순화하기 위해 RuntimeException으로 감싸 전파합니다.
            throw new RuntimeException(e);
        } finally {
            closeSafely(connection);
        }
    }

    private void rollbackSafely(final Connection connection) {
        if (connection != null) {
            try {
                connection.rollback();
            } catch (final SQLException rollbackEx) {
                log.warn("Rollback failed after transaction error", rollbackEx);
            }
        }
    }

    private void commitSafely(final Connection connection) {
        if (connection != null) {
            try {
                connection.commit();
            } catch (final SQLException commitEx) {
                log.warn("Commit failed after checked exception", commitEx);
            }
        }
    }

    private void closeSafely(final Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (final SQLException closeEx) {
                log.warn("Failed to close connection after transaction", closeEx);
            }
        }
    }
}
