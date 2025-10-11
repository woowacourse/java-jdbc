package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;

    public UserService(final UserDao userDao, final UserHistoryDao userHistoryDao) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
    }

    public User findById(final long id) {
        return userDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보가 존재하지 않습니다."));
    }

    public void insert(final User user) {
        userDao.insert(user);
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        try (Connection connection = DataSourceConfig.getInstance().getConnection()) {
            connection.setAutoCommit(false);
            try {
                final var user = userDao.findById(connection, id)
                        .orElseThrow(() -> new IllegalArgumentException("사용자 정보가 존재하지 않습니다."));
                user.changePassword(newPassword);
                userDao.update(connection, user);
                userHistoryDao.log(connection, new UserHistory(user, createBy));
                connection.commit();
            } catch (Exception e) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    log.error("Rollback failed", ex);
                }
                throw e;
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }
}
