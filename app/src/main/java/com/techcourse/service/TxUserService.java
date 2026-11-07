package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.domain.User;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public class TxUserService implements UserService {

    private final UserService userService;
    private final DataSource dataSource;

    public TxUserService(final UserService userService, final DataSource dataSource) {
        this.userService = userService;
        this.dataSource = dataSource;
    }

    @Override
    public User findById(final long id) {
        return userService.findById(id);
    }

    @Override
    public void save(final User user) {
        userService.save(user);
    }

    // override 대상인 메서드는 userService의 메서드를 그대로 위임(delegate)한다.
    @Override
    public void changePassword(final long id, final String newPassword, final String createdBy) {
        Connection connection = DataSourceUtils.getConnection(dataSource);

        try {
            connection.setAutoCommit(false);

            userService.changePassword(id, newPassword, createdBy);

            connection.commit();
        } catch (final SQLException | RuntimeException e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackException) {
                e.addSuppressed(rollbackException);
            }
            if (e instanceof SQLException sqlException) {
                throw new DataAccessException(sqlException);
            }
            throw (RuntimeException) e;
        } finally {
            try {
                DataSourceUtils.releaseConnection(connection, dataSource);
            } finally {
                TransactionSynchronizationManager.unbindResource(dataSource);
            }
        }
    }
}
