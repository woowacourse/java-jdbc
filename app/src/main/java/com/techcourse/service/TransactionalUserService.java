package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.techcourse.config.DataSourceConfig;
import java.sql.Connection;
import java.sql.SQLException;

public class TransactionalUserService implements UserService {

    private final UserService userService;

    public TransactionalUserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void changePassword(long id, String newPassword, String createBy) throws SQLException {
        Connection connection = DataSourceUtils.getConnection(DataSourceConfig.getInstance());
        try {
            connection.setAutoCommit(false);
            userService.changePassword(id, newPassword, createBy);
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw new DataAccessException(e);
        } finally {
            DataSourceUtils.releaseConnection(connection, DataSourceConfig.getInstance());
        }
    }
}
