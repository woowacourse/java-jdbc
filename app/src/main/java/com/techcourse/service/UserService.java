package com.techcourse.service;

import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.support.ConnectionManager;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.sql.Connection;

public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;
    private final DataSource dataSource;

    public UserService(UserDao userDao, UserHistoryDao userHistoryDao, DataSource dataSource) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
        this.dataSource = dataSource;
    }

    public User findById(final long id) {
        return userDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 아이디의 회원이 존재하지 않습니다."));
    }

    public void insert(final User user) {
        userDao.insert(user);
    }

    public void changePassword2(final long id, final String newPassword, final String createBy) {
        final var user = findById(id);
        user.changePassword(newPassword);

        Connection connection = null;
        try {
            connection = DataSourceUtils.getConnection(dataSource);
            connection.setAutoCommit(false);

            userDao.update(user);
            userHistoryDao.log(new UserHistory(user, createBy));

            connection.commit();
        } catch (Exception e) {
            ConnectionManager.rollback(e, connection);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        TransactionSynchronizationManager.execute(dataSource, () -> {
            final var user = findById(id);
            user.changePassword(newPassword);
            userDao.update(user);
            userHistoryDao.log(new UserHistory(user, createBy));
        });
    }
}
