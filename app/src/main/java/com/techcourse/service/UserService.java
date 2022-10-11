package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;
import nextstep.jdbc.exception.DataAccessException;

public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;

    public UserService(final UserDao userDao, final UserHistoryDao userHistoryDao) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
    }

    public User findById(final long id) {
        return userDao.findById(id);
    }

    public void insert(final User user) {
        userDao.insert(user);
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        final var user = findById(id);
        user.changePassword(newPassword);

        Connection connection = null;
        try {
            // 트랜잭션 시작
            connection = getConnection();
            connection.setAutoCommit(false);

            // 비즈니스 로직 처리
            userDao.update(connection, user);
            userHistoryDao.log(connection, new UserHistory(user, createBy));

            // 트랜잭션 커밋
            connection.commit();
        } catch (SQLException e) {
            // 트랜잭션 롤백
            try {
                Objects.requireNonNull(connection).rollback();
            } catch (SQLException ex) {
                throw new DataAccessException(e);
            }
            throw new DataAccessException(e);
        }
    }

    private Connection getConnection() throws SQLException {
        return DataSourceConfig.getInstance().getConnection();
    }
}
