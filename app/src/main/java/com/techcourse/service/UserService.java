package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import com.techcourse.exception.TechCourseApplicationException;
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
        return userDao.findById(id);
    }

    public void insert(final User user) {
        userDao.insert(user);
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        Connection connection = DataSourceConfig.getConnection();
        try (connection) {
            connection.setAutoCommit(false);

            final User passwordChanged = findById(id).changePassword(newPassword);
            userDao.update(connection, passwordChanged);
            userHistoryDao.log(connection, new UserHistory(passwordChanged, createBy));

            connection.commit();
        } catch (Exception e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                throw new TechCourseApplicationException("데이터를 롤백하는 것에 실패했습니다", ex);
            }
            throw new TechCourseApplicationException("비밀번호를 변경하는 것에 실패했습니다", e);
        }
    }
}
