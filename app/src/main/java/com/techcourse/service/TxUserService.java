package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public class TxUserService implements UserService {

    private final UserService userService;
    private final DataSource dataSource;

    public TxUserService(UserService userService, DataSource dataSource) {
        this.userService = userService;
        this.dataSource = dataSource;
    }

    public TxUserService(UserService userService) {
        this(userService, DataSourceConfig.getInstance());
    }

    @Override
    public User findById(long id) {
        return userService.findById(id);
    }

    @Override
    public void insert(User user) {
        try {
            Connection connection = DataSourceUtils.getConnection(dataSource);
            try {
                connection.setAutoCommit(false);
                userService.insert(user);
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw new DataAccessException("datasource접근 과정에서 예외가 발생했습니다.", e);
            }
        } catch (SQLException e) {
            throw new DataAccessException("롤백 과정에서 예외가 발생했습니다", e);
        }
    }

    @Override
    public void changePassword(long id, String newPassword, String createBy) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            try {
                connection.setAutoCommit(false);
                userService.changePassword(id, newPassword, createBy);
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw new DataAccessException("datasource접근 과정에서 예외가 발생했습니다.", e);
            }
        } catch (SQLException e) {
            throw new DataAccessException("롤백 과정에서 예외가 발생했습니다", e);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }
}
