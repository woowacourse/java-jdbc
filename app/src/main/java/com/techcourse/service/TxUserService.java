package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.exception.DataAccessException;
import org.springframework.jdbc.exception.RollbackFailException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class TxUserService {

    private final UserService userService;
    private final DataSource dataSource;

    public TxUserService(final UserService userService) {
        this.userService = userService;
        this.dataSource = DataSourceConfig.getInstance();
    }

    public User findById(final long id) {
        return userService.findById(id);
    }

    public void insert(final User user) {
        userService.insert(user);
    }

    public void changePassword(final long id, final String newPassword, final String createBy) throws SQLException {
        final Connection connection = DataSourceUtils.getConnection(dataSource);
        connection.setAutoCommit(false);
        try {
            userService.changePassword(id, newPassword, createBy);
            connection.commit();
        } catch (SQLException e) {
            rollback(connection);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    private static void rollback(Connection connection) {
        try {
            connection.rollback();
            throw new DataAccessException("데이터에 접근할 수 없습니다.");
        } catch (SQLException exception) {
            throw new RollbackFailException("롤백을 실패했습니다.");
        }
    }
}
