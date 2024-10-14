package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

public class TransactionUserService implements UserService {

    private final UserService userService;

    public TransactionUserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public Optional<User> findById(long id) {
        return userService.findById(id);
    }

    @Override
    public void save(User user) {
        executeTransaction(() -> userService.save(user));
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createdBy) {
        executeTransaction(() -> userService.changePassword(id, newPassword, createdBy));
    }

    private void executeTransaction(Runnable action) {
        DataSource dataSource = DataSourceConfig.getInstance();
        Connection connection = DataSourceUtils.getConnection(dataSource);

        try {
            tryCommit(action, connection);
        } catch (SQLException e) {
            tryRollback(e, connection);
            throw new DataAccessException("트랜잭션 수행 실패", e);
        } finally {
            DataSourceUtils.releaseConnection(dataSource);
        }
        setAutoCommitTrue(connection);
    }

    private void tryCommit(Runnable action, Connection connection) throws SQLException {
        connection.setAutoCommit(false);
        action.run();
        connection.commit();
    }

    private void tryRollback(SQLException e, Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException exception) {
            throw new DataAccessException("트랜잭션 롤백 실패", e);
        }
    }

    private void setAutoCommitTrue(Connection connection) {

    }
}
