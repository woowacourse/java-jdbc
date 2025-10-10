package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;
    private final DataSource dataSource;

    public UserService(final UserDao userDao, final UserHistoryDao userHistoryDao) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
        dataSource = DataSourceConfig.getInstance();
    }

    public User findById(final long id) {
        return userDao.findById(id)
                .orElseThrow(() -> new IllegalStateException("멤버가 존재하지 않습니다."));
    }

    public void insert(final User user) {
        userDao.insert(user);
    }

    public void changePassword(long id, String newPassword, String createdBy) {
        try (Connection connection = dataSource.getConnection()) {
            boolean old = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                User user = userDao.findByIdWithConnection(id, connection)
                        .orElseThrow(() -> new IllegalStateException("멤버가 존재하지 않습니다."));

                user.changePassword(newPassword);
                userDao.updateWithConnection(user, connection);
                userHistoryDao.log(new UserHistory(user, createdBy), connection);

                connection.commit();
            } catch (Exception e) {
                try {
                    connection.rollback();
                } catch (Exception ignore) {
                }
                throw e;
            } finally {
                try {
                    connection.setAutoCommit(old);
                } catch (Exception ignore) {
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
