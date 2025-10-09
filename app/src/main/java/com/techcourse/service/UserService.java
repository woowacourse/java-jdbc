package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import java.sql.SQLException;
import javax.sql.DataSource;

public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;
    private final DataSource dataSource;

    public UserService(final UserDao userDao, final UserHistoryDao userHistoryDao) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
        this.dataSource = null;
    }

    public UserService(final UserDao userDao, final UserHistoryDao userHistoryDao, final DataSource dataSource) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
        this.dataSource = dataSource;
    }

    public User findById(final long id) {
        return userDao.findById(id);
    }

    public void insert(final User user) {
        userDao.insert(user);
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        if (dataSource == null) {
            // 기존 방식 (트랜잭션 없음) - 호환성을 위해 유지
            final var user = findById(id);
            user.changePassword(newPassword);
            userDao.update(user);
            userHistoryDao.log(new UserHistory(user, createBy));
            return;
        }

        // 트랜잭션 적용
        applyTransaction(id, newPassword, createBy);
    }

    private void applyTransaction(long id, String newPassword, String createBy) {
        try (final var connection = dataSource.getConnection()) {
            // 트랜잭션 시작
            connection.setAutoCommit(false);

            try {
                // 비즈니스 로직 처리
                final var user = findById(id);
                user.changePassword(newPassword);
                userDao.update(connection, user);
                userHistoryDao.log(connection, new UserHistory(user, createBy));

                // 트랜잭션 커밋
                connection.commit();
            } catch (Exception e) {
                // 트랜잭션 롤백
                // 로직 처리 중에 예외가 발생하면 원자성을 보장하기 위해 롤백한다.
                connection.rollback();
                throw new DataAccessException(e);
            }
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }
}
