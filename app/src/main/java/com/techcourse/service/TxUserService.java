package com.techcourse.service;

import com.techcourse.domain.User;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class TxUserService implements UserService {

    private final DataSource dataSource;
    private final UserService userService;

    public TxUserService(
            final DataSource dataSource,
            final UserService userService) {
        this.dataSource = dataSource;
        this.userService = userService;
    }

    @Override
    public User findById(final long id) throws SQLException {
        final Connection conn = DataSourceUtils.getConnection(dataSource);
        conn.setAutoCommit(false);
        try {
            final User user = userService.findById(id);

            conn.commit();
            return user;
        } catch (SQLException e) {
            conn.rollback();
            throw new DataAccessException(e);
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }

    @Override
    public void insert(final User user) throws SQLException {
        final Connection conn = DataSourceUtils.getConnection(dataSource);
        conn.setAutoCommit(false);
        try {
            userService.insert(user);

            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw new DataAccessException(e);
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) throws SQLException {
        final Connection conn = DataSourceUtils.getConnection(dataSource);
        conn.setAutoCommit(false);
        try {
            userService.changePassword(id, newPassword, createBy);

            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw new DataAccessException(e);
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }
}
