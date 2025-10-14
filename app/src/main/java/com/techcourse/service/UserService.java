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
    private final DataSource dataSource;

    public UserService(final UserDao userDao, final UserHistoryDao userHistoryDao) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
        this.dataSource = DataSourceConfig.getInstance();
    }

    public User findById(final Connection conn, final long id) {
        return userDao.findById(conn, id);
    }

    // todo : Connection 리팩토링 이후 주석 삭제 예정
//    public void insert(final User user) {
//        userDao.insert(user);
//    }

    public void changePassword(final long id, final String newPassword, final String createBy) {

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);

            try {
                final var user = findById(conn, id);
                user.changePassword(newPassword);
                userDao.update(conn, user);

                userHistoryDao.log(conn, new UserHistory(user, createBy));
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        } catch (DataAccessException | SQLException e) {
            throw new DataAccessException("비밀번호 변경 실패", e);
        }
    }
}
