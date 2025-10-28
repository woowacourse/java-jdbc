package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.techcourse.domain.User;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Supplier;

public class TxUserService implements UserService {

    private final UserService userService;
    private final DataSource dataSource;

    public TxUserService(UserService userService, DataSource dataSource) {
        this.userService = userService;
        this.dataSource = dataSource;
    }

    @Override
    public User findById(long id) {
        return userService.findById(id);
    }

    @Override
    public void save(User user) {
        userService.save(user);
    }

    // override 대상인 메서드는 userService의 메서드를 그대로 위임(delegate)한다.
    @Override
    public void changePassword(final long id, final String newPassword, final String createdBy) {
        executeInTransaction(() -> {
            userService.changePassword(id, newPassword, createdBy);
            return null;
        });
    }

    private <T> T executeInTransaction(Supplier<T> action) {
        final Connection connection = DataSourceUtils.getConnection(dataSource);
        boolean originalAutoCommit = true;

        try {
            originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            try {
                T result = action.get();
                connection.commit();
                return result;
            } catch (Exception e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new DataAccessException(e);
        } finally {
            try {
                connection.setAutoCommit(originalAutoCommit);
            } catch (SQLException e) {
            }
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }
}
