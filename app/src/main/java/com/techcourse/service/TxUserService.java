package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.domain.User;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public class TxUserService implements UserService {

    private final UserService userService;
    private final DataSource dataSource;

    public TxUserService(final DataSource dataSource, final UserService userService) {
        this.dataSource = dataSource;
        this.userService = userService;
    }

    @Override
    public User findById(final long id) {
        return userService.findById(id);
    }

    @Override
    public void save(final User user) {
        userService.save(user);
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        try {
            changePasswordWithTransaction(id, newPassword, createBy);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private void changePasswordWithTransaction(final long id, final String newPassword,
                                               final String createBy) throws SQLException {
        Connection connection = TransactionSynchronizationManager.getResource(dataSource);
        connection.setAutoCommit(false);

        try {
            userService.changePassword(id, newPassword, createBy);
            connection.commit();
        } catch (Throwable e) {
            connection.rollback();
        } finally {
            connection.setAutoCommit(true);
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }
}
