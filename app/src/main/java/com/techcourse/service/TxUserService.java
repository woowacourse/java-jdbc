package com.techcourse.service;

import java.sql.SQLException;

import javax.sql.DataSource;

import com.interface21.dao.DataAccessException;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;

public class TxUserService implements UserService {

    private final UserService userService;

    public TxUserService(final UserService userService) {
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
        final DataSource dataSource = getDataSource();
        try (final var connection = TransactionSynchronizationManager.getResource(dataSource)) {
            connection.setAutoCommit(false);

            try {
                userService.changePassword(id, newPassword, createdBy);
                connection.commit();
            } catch (Exception e) {
                connection.rollback();
                throw e;
            } finally {
                TransactionSynchronizationManager.unbindResource(dataSource);
                connection.setAutoCommit(true);
            }
        } catch (Exception e) {
            throw new BusinessException("비밀번호 변경에 실패했습니다.", e);
        }
    }

    private DataSource getDataSource() {
        try {
            final DataSource dataSource = DataSourceConfig.getInstance();
            TransactionSynchronizationManager.bindResource(dataSource, dataSource.getConnection());
            return dataSource;
        } catch (SQLException e) {
            throw new DataAccessException("", e);
        }
    }
}
