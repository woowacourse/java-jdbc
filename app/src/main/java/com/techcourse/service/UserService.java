package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.NoSuchElementException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;
    private final DataSource dataSource;

    public UserService(final UserDao userDao, final UserHistoryDao userHistoryDao, final DataSource dataSource) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
        this.dataSource = dataSource;
    }

    public void insert(final User user) {
        userDao.insert(user);
    }

    public User findById(final long id) {
        return userDao.findById(id)
                .orElseThrow(NoSuchElementException::new);
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);

            try {
                final var user = findById(connection, id);
                user.changePassword(newPassword);
                userDao.update(connection, user);
                userHistoryDao.log(connection, new UserHistory(user, createBy));

                connection.commit();
            } catch (Exception e) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackEx) {
                    e.addSuppressed(rollbackEx);
                }
                throw new DataAccessException("비밀번호 변경 실패", e);
            }
        } catch (SQLException e) {
            throw new DataAccessException("비밀번호 변경 실패", e);
        }
    }

    private User findById(final Connection connection, final long id) {
        return userDao.findById(connection, id)
                .orElseThrow(NoSuchElementException::new);
    }
}
