package com.techcourse.service;

import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
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

    public User findById(final long id) {
        return userDao.findById(id)
                .orElseThrow(() -> new IllegalStateException("멤버가 존재하지 않습니다."));
    }

    public void insert(final User user) {
        userDao.insert(user);
    }

    public void changePassword(long id, String newPassword, String createdBy) {
        inTransaction(conn -> changePasswordTx(conn, id, newPassword, createdBy));
    }

    private void changePasswordTx(Connection conn, long id, String newPassword, String createdBy) {
        User user = findUserForUpdate(conn, id);
        user.changePassword(newPassword);
        userDao.updateWithConnection(user, conn);
        userHistoryDao.log(new UserHistory(user, createdBy), conn);
    }

    private User findUserForUpdate(Connection conn, long id) {
        return userDao.findByIdWithConnection(id, conn)
                .orElseThrow(() -> new IllegalStateException("멤버가 존재하지 않습니다."));
    }

    @FunctionalInterface
    private interface SqlConsumer<T> {
        void accept(T t) throws Exception;
    }

    private void inTransaction(SqlConsumer<Connection> work) {
        try (Connection connection = DataSourceUtils.getConnection(dataSource)) {
            connection.setAutoCommit(false);
            try {
                work.accept(connection);
                connection.commit();
            } catch (Exception ex) {
                connection.rollback();
                throwUncheckedException(ex);
            } finally {
                DataSourceUtils.releaseConnection(connection, dataSource);
                TransactionSynchronizationManager.unbindResource(dataSource);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void throwUncheckedException(Exception ex) {
        if (ex instanceof RuntimeException re) {
            throw re;
        }
        throw new RuntimeException(ex);
    }
}
