package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;

    public UserService(final UserDao userDao, final UserHistoryDao userHistoryDao) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
    }

    public User findById(final long id) {
        return userDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("[ERROR] 존재하지 않는 사용자입니다. id: " + id));
    }

    public void insert(final User user) {
        userDao.insert(user);
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        final var user = findById(id);
        user.changePassword(newPassword);

        final DataSource dataSource = DataSourceConfig.getInstance();
        try (final Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false); // 자동 확정 끄기
            try {
                if (userDao.update(conn, user) == 0) { // 같은 커넥션 사용
                    throw new IllegalArgumentException("[ERROR] 존재하지 않는 사용자입니다. id: " + id);
                }
                userHistoryDao.log(conn, new UserHistory(user, createBy));
                conn.commit(); // 둘 다 성공이면 확정
            } catch (SQLException | RuntimeException e) {
                conn.rollback(); // 하나라도 실패라면 취소
                throw e;
            }
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }
}
