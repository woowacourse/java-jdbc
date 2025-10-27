package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public final class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;
    private final DataSource dataSource;

    public UserService(
            final UserDao userDao,
            final UserHistoryDao userHistoryDao,
            final DataSource dataSource
    ) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
        this.dataSource = dataSource;
    }

    public User findById(final long id) {
        return userDao.findById(id);
    }

    public void insert(final User user) {
        userDao.insert(user);
    }

    public void changePassword(
            final long id,
            final String newPassword,
            final String createBy
    ) {
        final User user = findById(id);
        user.changePassword(newPassword);
        try (final Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                userDao.update(connection, user);
                userHistoryDao.log(connection, new UserHistory(user, createBy));
                connection.commit();
            } catch (final Exception e) {
                connection.rollback();
                throw new DataAccessException("비밀번호 변경 중 오류가 발생했습니다.", e);
            }
        } catch (final SQLException e) {
            throw new DataAccessException("데이터베이스 연결 중 오류가 발생했습니다.", e);
        }
    }
}
