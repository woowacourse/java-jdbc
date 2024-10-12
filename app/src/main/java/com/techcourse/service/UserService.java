package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import com.techcourse.support.SQLExceptionConsumer;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public class UserService {

    private UserDao userDao;
    private UserHistoryDao userHistoryDao;
    private DataSource dataSource;

    public UserService(DataSource datasource, UserDao userDao, UserHistoryDao userHistoryDao) {
        this.dataSource = datasource;
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
        Connection connection = SQLExceptionConsumer.execute(dataSource::getConnection, "connetion을 가져오는데 실패했습니다");
        try {
            connection.setAutoCommit(false);
            doChangePassword(id, newPassword, createBy, connection);
            connection.commit();
        } catch (SQLException e) {
            rollbackTransaction(connection);
            throw new DataAccessException(e);
        } finally {
            closeConnection(connection);
        }
    }

    private void closeConnection(Connection connection) {
        SQLExceptionConsumer.execute(() -> {
            connection.close();
            return null;
        }, "connection을 닫는데 실패했습니다.");
    }

    private void rollbackTransaction(Connection connection) {
        SQLExceptionConsumer.execute(() -> {
            connection.rollback();
            return null;
        }, "connection을 rollback하는데 실패했습니다.");
    }


    private void doChangePassword(long id, String newPassword, String createBy, Connection connection) {
        User user = findById(id);
        user.changePassword(newPassword);
        userDao.update(connection, user);
        userHistoryDao.log(connection, new UserHistory(user, createBy));
    }
}
