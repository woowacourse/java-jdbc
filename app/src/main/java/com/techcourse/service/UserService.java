package com.techcourse.service;

import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.dao.DataAccessException;

public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;
    private final DataSource dataSource;

    public UserService(final UserDao userDao, final UserHistoryDao userHistoryDao, final DataSource dataSource) {
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
        Connection connection = null;
        // 하나의 Connection 을 공유하도록 해야한다.
        try {
            // TODO: 2023-10-05 Connection 을 가져오고, commit/rollback, close 하는 부분 로직 중복 처리
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            // 아래 라인에서 실제 실행할 로직만 두는 것
            executeChangePassword(id, newPassword, createBy, connection);
            connection.commit();
        } catch (Exception e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    throw new DataAccessException(ex);
                }
            }
            throw new DataAccessException();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    throw new DataAccessException(e);
                }
            }
        }
    }

    private void executeChangePassword(final long id, final String newPassword, final String createBy, final Connection connection) {
        final var user = findById(id);
        user.changePassword(newPassword);
        // TODO: 2023-10-05 DAO가 Connection을 몰라도 되도록 수정하기
        userDao.update(connection, user);
        userHistoryDao.log(connection, new UserHistory(user, createBy));
    }
}
