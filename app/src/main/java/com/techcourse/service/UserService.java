package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.manager.TransactionManager;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private static final String PASSWORD_ERROR_MESSAGE = "비밀번호를 수정하던 도중 에러가 발생했습니다";

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;
    private final DataSource dataSource;

    public UserService(UserDao userDao, UserHistoryDao userHistoryDao) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
        this.dataSource = DataSourceConfig.getInstance();
    }

    public User findByIdWithTransaction(Connection connection, long id) {
        return userDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
    }

    public User findById(long id) {
        return userDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
    }

    public Optional<User> findByAccount(String account) {
        return userDao.findByAccount(account);
    }

    public void insert(User user) {
        userDao.insert(user);
    }

    public void changePasswordWithTransaction(long id, String newPassword, String createBy) {
        try (Connection connection = dataSource.getConnection()) {
            changePassword(id, newPassword, createBy, connection);
        } catch (SQLException | DataAccessException e) {
            log.error(PASSWORD_ERROR_MESSAGE + " : {}", e.getMessage());
            throw new DataAccessException(PASSWORD_ERROR_MESSAGE, e);
        }
    }

    private void changePassword(long id, String newPassword, String createBy, Connection connection) {
        TransactionManager.start(connection, () -> {
            final var user = findByIdWithTransaction(connection, id);
            user.changePassword(newPassword);
            userDao.updateWithTransaction(connection, user);
            userHistoryDao.insertWithTransaction(connection, new UserHistory(user, createBy));
        });
    }
}
