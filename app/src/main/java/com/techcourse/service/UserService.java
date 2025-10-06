package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
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
        try (final var connection = DataSourceConfig.getInstance().getConnection()) {
            // 트랜잭션 시작
            connection.setAutoCommit(false);
            try {
                // 비즈니스 로직 처리
                final var user = findById(id);
                user.changePassword(newPassword);
                userDao.update(user);
                userHistoryDao.log(new UserHistory(user, createBy));
            } catch (Exception e) {
                // 트랜잭션 롤백
                // 로직 처리 중에 예외가 발생하면 원자성을 보장하기 위해 롤백한다.
                connection.rollback(); // try-catch로 한 번 더 감싸야 하지만 예시니까 생략
                throw new DataAccessException(e);
            }

            // 트랜잭션 커밋
            connection.commit();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }
}
