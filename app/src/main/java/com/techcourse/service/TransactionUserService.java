package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

public class TransactionUserService implements UserService {

    private final UserService userService;

    public TransactionUserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public Optional<User> findById(long id) {
        return userService.findById(id);
    }

    @Override
    public void save(User user) {
        DataSource dataSource = DataSourceConfig.getInstance();
        Connection connection = DataSourceUtils.getConnection(dataSource);

        try {
            connection.setAutoCommit(false);
            userService.save(user);
            connection.commit();
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException exception) {
                throw new DataAccessException("트랜잭션 롤백 실패", e);
            }
            throw new DataAccessException("트랜잭션 수행 실패", e);
        } finally {
            DataSourceUtils.releaseConnection(dataSource);
        }
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createdBy) {
        DataSource dataSource = DataSourceConfig.getInstance();
        Connection connection = DataSourceUtils.getConnection(dataSource);

        try {
            connection.setAutoCommit(false);
            userService.changePassword(id, newPassword, createdBy);
            connection.commit();
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException exception) {
                throw new DataAccessException("트랜잭션 롤백 실패", e);
            }
            throw new DataAccessException("트랜잭션 수행 실패", e);
        } finally {
            DataSourceUtils.releaseConnection(dataSource);
        }
    }
}
