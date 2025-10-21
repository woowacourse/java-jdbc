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
        try (final var connection = dataSource.getConnection()) {
            // Connection Pool에서 재사용되는 경우를 대비해 원래 autoCommit 상태를 저장
            final boolean originalAutoCommit = connection.getAutoCommit();

            try {
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
            } finally {
                // Connection을 Pool로 반환하기 전에 원래 autoCommit 상태로 복원
                // 이를 통해 다음 요청에서 이 Connection을 재사용할 때 예상치 못한 동작 방지
                connection.setAutoCommit(originalAutoCommit);
            }
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }
}
