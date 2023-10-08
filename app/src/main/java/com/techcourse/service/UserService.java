package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.NoSuchElementException;
import javax.sql.DataSource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.exception.TransactionAutoCommitException;
import org.springframework.transaction.exception.TransactionCommitException;
import org.springframework.transaction.exception.TransactionRollbackException;

public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;

    public UserService(final UserDao userDao, final UserHistoryDao userHistoryDao) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
    }

    public User findById(final long id) {
        return userDao.findById(id)
                .orElseThrow(() -> new NoSuchElementException("해당 유저를 찾을 수 없습니다."));
    }

    public void insert(final User user) {
        userDao.insert(user);
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        final DataSource dataSource = DataSourceConfig.getInstance();
        final Connection conn = DataSourceUtils.getConnection(dataSource);
        setAutoCommit(false, conn);

        try {
            final User user = findById(id);
            user.changePassword(newPassword);
            userDao.update(user);
            userHistoryDao.log(new UserHistory(user, createBy));

            commit(conn);
        } catch (final RuntimeException e) {
            rollback(conn);
            throw new DataAccessException(e);
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }

    private void setAutoCommit(final boolean autoCommit, final Connection conn) {
        try {
            conn.setAutoCommit(autoCommit);
            System.out.println("conn.getAutoCommit() = " + conn.getAutoCommit());
        } catch (final SQLException e) {
            throw new TransactionAutoCommitException(e);
        }
    }

    private void commit(final Connection conn) {
        try {
            conn.commit();
        } catch (final SQLException e) {
            throw new TransactionCommitException();
        }
    }

    private void rollback(final Connection conn) {
        try {
            conn.rollback();
            System.out.println("conn.getAutoCommit() in rollback = " + conn.getAutoCommit());
        } catch (final SQLException e) {
            throw new TransactionRollbackException(e);
        }
    }
}
