package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;

import javax.sql.DataSource;
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
        DataSource dataSource = DataSourceConfig.getInstance();
        try(Connection conn = dataSource.getConnection()){
            conn.setAutoCommit(false);
            try{
                userDao.insert(conn, user);
                conn.commit();
            }catch (Exception e) {
                rollback(conn, e);
                throw new DataAccessException("회원 저장 실패", e);
            }
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        DataSource dataSource = DataSourceConfig.getInstance();
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                final var user = findById(id);
                user.changePassword(newPassword);
                userDao.update(conn,user);
                userHistoryDao.log(conn, new UserHistory(user, createBy));
                conn.commit();
            } catch (Exception e) {
                rollback(conn, e);
                throw new DataAccessException("비밀번호 변경 실패", e);
            }
        }catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private void rollback(final Connection conn, final Exception cause) {
        try {
            conn.rollback();
        } catch (SQLException rollbackException) {
            cause.addSuppressed(rollbackException);
        }
    }
}
