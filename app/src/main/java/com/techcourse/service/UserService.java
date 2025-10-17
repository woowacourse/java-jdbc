package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;
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
        try (final Connection connection = DataSourceConfig.getInstance().getConnection()) {
            connection.setAutoCommit(false);

            try {
                final var user = findById(id);
                user.changePassword(newPassword);
                userDao.update(connection, user);
                userHistoryDao.log(connection, new UserHistory(user, createBy));

                connection.commit();
            } catch (final Exception e) {
                connection.rollback();
                throw e;
            }

        } catch (final Exception e) {
            throw new RuntimeException("비번변경실패", e);
        }
    }
}
