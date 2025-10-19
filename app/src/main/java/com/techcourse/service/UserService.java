package com.techcourse.service;

import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;

    public User findById(final long id) {
        return userDao.findById(id).orElseThrow();
    }

    public void insert(final User user) {
        userDao.insert(user);
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        final DataSource dataSource = DataSourceConfig.getInstance();
        try (final Connection connection = DataSourceUtils.getConnection(dataSource)) {
            TransactionSynchronizationManager.bindResource(dataSource, connection);

            try {
                final var user = findById(id);
                user.changePassword(newPassword);
                userDao.update(user);
                userHistoryDao.log(new UserHistory(user, createBy));

                connection.commit();
            } catch (final Exception e) {
                connection.rollback();
                throw e;
            } finally {
                TransactionSynchronizationManager.unbindResource(dataSource);
            }

        } catch (final Exception e) {
            throw new RuntimeException("비번변경실패", e);
        }
    }
}
