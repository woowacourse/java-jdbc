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
        DataSource dataSource = DataSourceConfig.getInstance();
        Connection conn = DataSourceUtils.getConnection(dataSource);
        User user = null;
        try {
            conn.setAutoCommit(false);
            user = userService.findById(id);
            conn.commit();
        } catch (SQLException e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException rollbackEx) {
                throw new DataAccessException("Rollback failed", rollbackEx);
            }
            throw new DataAccessException(e);
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
        return user;
    }

    @Override
    public void changePassword(long id, String newPassword, String createdBy) {
        DataSource dataSource = DataSourceConfig.getInstance();
        Connection conn = DataSourceUtils.getConnection(dataSource);
        try {
            conn.setAutoCommit(false);
            userService.changePassword(id, newPassword, createdBy);
            conn.commit();
        } catch (SQLException e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException rollbackEx) {
                throw new DataAccessException("Rollback failed", rollbackEx);
            }
            throw new DataAccessException(e);
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }
}
