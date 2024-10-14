package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;

import java.sql.Connection;
import java.sql.SQLException;

public class TxUserService implements UserService {

    private final UserService userService;

    public TxUserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public User findById(long id) {
        return userService.findById(id);
    }

    @Override
    public void save(User user) {
        userService.save(user);
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createdBy) {
        Connection connection = DataSourceUtils.getConnection(DataSourceConfig.getInstance());
        update(connection, id, newPassword, createdBy);
        DataSourceUtils.releaseConnection(connection, DataSourceConfig.getInstance());
    }

    private void update(Connection connection, long id, String newPassword, String createdBy) {
        try {
            connection.setAutoCommit(false);
            userService.changePassword(id, newPassword, createdBy);
            connection.commit();
        } catch (SQLException | DataAccessException e) {
            try {
                connection.rollback();
            } catch (SQLException sqlException) {
            }
            throw new DataAccessException();
        }
    }
}
