package com.techcourse.service;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;

public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;

    public UserService(final UserDao userDao, final UserHistoryDao userHistoryDao) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
    }

    public User findById(final long id) {
        log.info("{} 조회", id);
        return userDao.findById(id);
    }

    public void insert(final User user) {
        log.info("유저 저장 id = {}", user.getId());
        userDao.insert(user);
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        DataSource dataSource = DataSourceConfig.getInstance();
        Connection connection = getConnection(dataSource);
        try{
            connection.setAutoCommit(false);
            User user = findById(id);
            user.changePassword(newPassword);
            userDao.update(connection, user);
            userHistoryDao.log(connection, new UserHistory(user, createBy));
            connection.commit();
        } catch (SQLException e){
            throw new RuntimeException("비밀번호 변경중 오류가 발생했습니다.", e);
        }
    }

    private Connection getConnection(final DataSource dataSource) {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
