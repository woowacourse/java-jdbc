package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.jdbc.support.SQLExceptionTranslator;

public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;
    private final DataSource dataSource;

    public UserService(final UserDao userDao, final UserHistoryDao userHistoryDao) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
        this.dataSource = DataSourceConfig.getInstance();
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

        try (final Connection conn = dataSource.getConnection()) {
            try {
                conn.setAutoCommit(false);
                userDao.update(conn, user);
                userHistoryDao.log(conn, new UserHistory(user, createBy));
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw SQLExceptionTranslator.translate("", e);
        }
    }
}
