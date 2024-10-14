package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public class TxUserService implements UserService {

    private final AppUserService userService;
    private final DataSource dataSource;

    public TxUserService(AppUserService userService) {
        this.userService = userService;
        this.dataSource = DataSourceConfig.getInstance();
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
        try (Connection connection = DataSourceUtils.getConnection(dataSource)) {
            try {
                connection.setAutoCommit(false);
                userService.changePassword(id, newPassword, createdBy);
                connection.commit();
            } catch (Exception e) {
                connection.rollback();
                throw new DataAccessException(e);
            } finally {
                DataSourceUtils.releaseConnection(connection, dataSource);
            }
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }
}
