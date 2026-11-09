package com.techcourse.service;

import com.interface21.dao.DataAccessException;
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
        this(userDao, userHistoryDao, DataSourceConfig.getInstance());
    }

    public UserService(UserDao userDao, UserHistoryDao userHistoryDao, DataSource dataSource) {
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

    public void changePassword(final long id, final String newPassword, final String createBy) {
        final var user = findById(id);
        user.changePassword(newPassword);

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            updateWithHistory(conn, user, createBy);
            conn.commit();
        } catch (SQLException e) {
            throw new DataAccessException("비밀번호 변경 트랜잭션 처리에 실패했습니다.", e);
        }
    }

    private void updateWithHistory(Connection conn, User user, String createBy) throws SQLException {
        try {
            userDao.update(conn, user);
            userHistoryDao.log(conn, new UserHistory(user, createBy));
        } catch (RuntimeException | Error e) {
            conn.rollback();
            throw e;
        }
    }
}
