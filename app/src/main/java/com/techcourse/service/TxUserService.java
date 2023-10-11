package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.datasource.DataSourceUtils;

public class TxUserService implements UserService {

    private final UserService appUserService;

    public TxUserService(UserService appUserService) {
        this.appUserService = appUserService;
    }

    @Override
    public User findById(long id) {
        return appUserService.findById(id);
    }

    @Override
    public void insert(User user) {
        appUserService.insert(user);
    }

    @Override
    public void changePassword(long id, String newPassword, String createBy) {
        DataSource dataSource = DataSourceConfig.getInstance();
        Connection conn = DataSourceUtils.getConnection(dataSource);

        try {
            conn.setAutoCommit(false);
            appUserService.changePassword(id, newPassword, createBy);
            conn.commit();
        } catch (RuntimeException | SQLException e) {
            rollback(conn, e);
            throw new DataAccessException(e);
        } finally {
            close(dataSource);
        }
    }


    private void rollback(Connection conn, Exception e) {
        if (conn == null) {
            return;
        }
        try {
            conn.rollback();
        } catch (SQLException ignored) {
            throw new DataAccessException("롤백 실패", e);
        }
    }

    private void close(DataSource dataSource) {
        try {
            DataSourceUtils.releaseConnection(dataSource);
        } catch (CannotGetJdbcConnectionException e) {
        }
    }
}
