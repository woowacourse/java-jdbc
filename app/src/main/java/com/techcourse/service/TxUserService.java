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

    public TxUserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public User findById(long id) {
        User[] user = new User[1];
        doTransaction(() -> user[0] = userService.findById(id));
        return user[0];
    }

    @Override
    public void changePassword(long id, String newPassword, String createdBy) {
        doTransaction(() -> userService.changePassword(id, newPassword, createdBy));
    }

    private void doTransaction(Runnable runnable) {
        DataSource dataSource = DataSourceConfig.getInstance();
        try (Connection conn = DataSourceUtils.getConnection(dataSource)) {
            try {
                conn.setAutoCommit(false);
                runnable.run();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw new DataAccessException(e);
            } finally {
                DataSourceUtils.releaseConnection(conn, dataSource);
            }
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }
}
