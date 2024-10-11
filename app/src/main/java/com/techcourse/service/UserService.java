package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;
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
        return userDao.findById(id);
    }

    public void insert(final User user) {
        userDao.insert(user);
    }

    public void changePassword(long id, String newPassword, String createBy) {
        try (Connection connection = DataSourceConfig.getInstance().getConnection()){
            connection.setAutoCommit(false);

            User user = findById(id);
            user.changePassword(newPassword);
            userDao.update(connection, user);

            UserHistory userHistory = new UserHistory(user, createBy);
            userHistoryDao.log(connection, userHistory);

            connection.commit();
        } catch (Exception e) {
            log.info("CHANGE_PASSWORD_ERROR :: {}", e.getMessage(), e);
            throw new DataAccessException("비밀번호를 변경하던 중 예외가 발생했습니다.");
        }
    }
}
