package com.techcourse.service;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;

public class UserTxService implements UserService {

    private final UserService userService;

    public UserTxService(final UserService userService) {
        this.userService = userService;
    }

    @Override
    public User findById(final long id) {
        return userService.findById(id);
    }

    @Override
    public void insert(final User user) {
        userService.insert(user);
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        final DataSource instance = DataSourceConfig.getInstance();
        final Connection connection = DataSourceUtils.getConnection(instance);

        try {
            connection.setAutoCommit(false);
            userService.changePassword(id, newPassword, createBy);
            connection.commit();
        } catch (final Exception e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                throw new DataAccessException("데이터 롤백에 실패했습니다.");
            }
            throw new DataAccessException("데이터 접근에 실패했습니다.");
        } finally {
            DataSourceUtils.releaseConnection(connection, instance);
        }
    }
}
