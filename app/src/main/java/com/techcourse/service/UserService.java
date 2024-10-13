package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import com.techcourse.exception.UserNotFoundException;
import java.sql.Connection;
import java.sql.SQLException;

public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;

    public UserService(final UserDao userDao, final UserHistoryDao userHistoryDao) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
    }

    public User findById(final long id) {
        return userDao.findById(id)
                .orElseThrow(() -> new UserNotFoundException("유저 정보가 존재하지 않습니다."));
    }

    public void insert(final User user) {
        userDao.insert(user);
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        try (Connection connection = DataSourceConfig.getInstance().getConnection()) {
            changePasswordWithTransaction(id, newPassword, createBy, connection);
        } catch (SQLException e) {
            throw new DataAccessException("데이터 접근 과정에서 문제가 발생하였습니다.", e);
        }
    }

    private void changePasswordWithTransaction(long id, String newPassword, String createBy, Connection conn) {
        try {
            conn.setAutoCommit(false);
            final var user = findById(id);
            User changedUser = user.changePassword(newPassword);
            userDao.update(conn, changedUser);
            userHistoryDao.log(conn, new UserHistory(user, createBy));
            conn.commit();
        } catch (SQLException e) {
            tryRollBack(conn);
            throw new DataAccessException("트랜잭션 수행 중 문제가 발생하여 롤백하였습니다.", e);
        }
    }

    private void tryRollBack(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException e) {
            throw new DataAccessException("롤백 작업 수행 중 문제가 발생하였습니다.", e);
        }
    }
}
