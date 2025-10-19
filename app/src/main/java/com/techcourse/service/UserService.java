package com.techcourse.service;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;
import javax.xml.crypto.Data;

import com.interface21.dao.DataAccessException;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;

public class UserService {

    private final DataSource dataSource;
    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;

    public UserService(final UserDao userDao, final UserHistoryDao userHistoryDao) {
        this.dataSource = DataSourceConfig.getInstance();
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
    }

    public User findById(final long id) {
        return userDao.findById(id);
    }

    public void insert(final User user) {
        userDao.insert(user);
    }

    // Spring의 @Transactional을 사용하면 Thread Local에 Connection이 바인딩되어 같은 Connection을 사용하게 된다.
    // 하지만 현재는 학습 목적으로 @Transactional을 사용하지 않고 수동으로 같은 Connection을 사용하도록 구현하겠다.
    public void changePassword(final long id, final String newPassword, final String createBy) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);

            try {
                final var user = findById(id);
                user.changePassword(newPassword);
                userDao.update(connection, user);
                userHistoryDao.log(connection, new UserHistory(user, createBy));
            } catch (Exception e) { // 비즈니스 로직 실패
                try {
                    connection.rollback();
                    throw new DataAccessException("Transaction rolled back due to an error", e);
                } catch (SQLException ex) { // 롤백 실패
                    throw new DataAccessException("Failed to rollback transaction", ex);
                }
            }

            connection.commit();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to change password", e);
        }
    }
}
