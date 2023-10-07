package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import org.springframework.dao.DataAccessException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;

    public UserService(final UserDao userDao, final UserHistoryDao userHistoryDao) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
    }

    public Optional<User> findById(final long id) {
        return userDao.findById(id);
    }

    public void insert(final User user) {
        userDao.insert(user);
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        final DataSource dataSource = DataSourceConfig.getInstance();

        try (final Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);

            try {
                final var user = findById(id).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
                user.changePassword(newPassword);

                userDao.update(conn, user);
                userHistoryDao.log(conn, new UserHistory(user, createBy));

                conn.commit();
            } catch (final SQLException e) {
                conn.rollback();
                throw new DataAccessException(e);
            }
        } catch (final SQLException e) {
            throw new DataAccessException(e);
        }
    }
}
