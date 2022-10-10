package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;
import java.sql.SQLException;
import nextstep.jdbc.DataAccessException;

public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;

    public UserService(final UserDao userDao, final UserHistoryDao userHistoryDao) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
    }

    public User findById(final long id) {
        final Connection connection = generateConnection();

        return userDao.findById(connection, id);
    }

    public void insert(final User user) {
        final Connection connection = generateConnection();
        userDao.insert(connection, user);
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {

        final Connection connection = generateConnection();

        try {
            // 트랜잭션 시작
            connection.setAutoCommit(false);

            // 비즈니스 로직 처리
            final var user = findById(id);
            user.changePassword(newPassword);
            userDao.update(connection, user);
            userHistoryDao.log(connection, new UserHistory(user, createBy));

            // 트랜잭션 커밋
            connection.commit();
        } catch (SQLException e) {
            // 트랜잭션 롤백
            // 로직 처리 중에 예외가 발생하면 원자성을 보장하기 위해 롤백한다.
            rollback(connection);
            throw new DataAccessException(e);
        }
    }

    private void rollback(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private Connection generateConnection() {
        try {
            return DataSourceConfig.getInstance().getConnection();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }
}
