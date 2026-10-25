package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import java.sql.Connection;
import java.sql.SQLException;

public class TxUserService implements UserService {

    private final UserService userService;

    public TxUserService(final UserService userService) {
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
    public void changePassword(long id, String newPassword, String createdBy) {
        Connection connection = DataSourceUtils.getConnection(DataSourceConfig.getInstance());
        try {
            connection.setAutoCommit(false);
            userService.changePassword(id, newPassword, createdBy);
            connection.commit();
        } catch (SQLException | DataAccessException e) {
            rollback(e, connection);
            throw new DataAccessException(e.getMessage());
        } finally {
            close(connection);
        }
    }

    private void rollback(Exception e, Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException ex) {
            e.addSuppressed(ex);
        }
    }

    private void close(Connection connection) {
        DataSourceUtils.releaseConnection(connection, DataSourceConfig.getInstance());
    }
}
