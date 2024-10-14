package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.jdbc.exception.DataQueryException;
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
    public void changePassword(long id, String newPassword, String createBy) {
        Connection connection = DataSourceUtils.getConnection(DataSourceConfig.getInstance());
        try {
            connection.setAutoCommit(false);

            userService.changePassword(id, newPassword, createBy);

            connection.commit();
        } catch (Exception e) {
            handleException(connection);
            if (e instanceof SQLException) {
                throw new DataQueryException(e.getMessage(), e);
            }
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    private void handleException(Connection connection) {
        if (connection != null) {
            handleRollBack(connection);
        }
    }

    private void handleRollBack(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException rollbackEx) {
            throw new DataQueryException(rollbackEx.getMessage(), rollbackEx);
        }
    }
}
