package com.techcourse.service;

import com.interface21.jdbc.CannotGetJdbcConnectionException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class TransactionalUserService implements UserService {

    private final DataSource dataSource = DataSourceConfig.getInstance();
    private final UserService userService;

    public TransactionalUserService(final UserService userService) {
        this.userService = userService;
    }

    @Override
    public User findById(final long id) {
        return userService.findById(id);
    }

    @Override
    public void save(final User user) {
        userService.save(user);
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createdBy) {
        try (Connection connection = DataSourceUtils.getConnection(dataSource)) {
            try {
                connection.setAutoCommit(false);
                userService.changePassword(id, newPassword, createdBy);
                connection.commit();
            } catch (Exception e) {
                try {
                    connection.rollback();
                } catch (SQLException sqlException) {
                    throw new RuntimeException("트랜잭션 롤백에 실패했습니다.", sqlException);
                }
            }
        } catch (CannotGetJdbcConnectionException | SQLException e) {
            throw new RuntimeException(dataSource.toString() + "로부터 커넥션을 획득하는 데에 실패했습니다.", e);
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }
}
