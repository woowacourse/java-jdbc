package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;

import javax.sql.DataSource;
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
        User user = findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저:" + id));
        DataSource dataSource = DataSourceConfig.getInstance();
        try (final var connection = dataSource.getConnection()) {
            try {
                connection.setAutoCommit(false);
                user.changePassword(newPassword);
                userDao.update(connection, user);
                userHistoryDao.log(connection, new UserHistory(user, createBy));
            } catch (SQLException e) {
                connection.rollback();
                throw new DataAccessException("트랜잭션 중 문제 발생으로 롤백 수행", e);
            }
            connection.commit();
        } catch (SQLException e) {
            throw new DataAccessException("트랜잭션 수행 실패", e);
        }
    }
}
