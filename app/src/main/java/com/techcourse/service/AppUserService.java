package com.techcourse.service;

import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.NoSuchElementException;

public class AppUserService implements UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;
    private final DataSource dataSource;

    public AppUserService(
            final UserDao userDao,
            final UserHistoryDao userHistoryDao,
            final DataSource dataSource
    ) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
        this.dataSource = dataSource;
    }

    @Override
    public User findById(final long id) {
        return userDao.findById(id)
                .orElseThrow(() -> new NoSuchElementException("해당 아이디의 사용자가 존재하지 않습니다."));
    }

    @Override
    public void insert(final User user) throws SQLException {
        final Connection conn = DataSourceUtils.getConnection(dataSource);
        conn.setAutoCommit(false);
        try {
            userDao.insert(user);

            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw new DataAccessException(e);
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        final var user = findById(id);
        user.changePassword(newPassword);
        userDao.update(user);
        userHistoryDao.log(new UserHistory(user, createBy));
    }
}
