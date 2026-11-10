package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public class TxUserService implements UserService {

    private final UserService userService;
    private final DataSource dataSource;

    public TxUserService(UserService userService) {
        this.userService = userService;
        this.dataSource = DataSourceConfig.getInstance();
    }

    @Override
    public User findById(long id) {
        return userService.findById(id);
    }

    @Override
    public void save(User user) {
        userService.save(user);
    }

    @Override
    public void changePassword(long id, String newPassword, String createdBy) {
        final Connection conn = DataSourceUtils.getConnection(dataSource);
        try {
            conn.setAutoCommit(false);

            userService.changePassword(id, newPassword, createdBy);

            conn.commit();
        } catch (final RuntimeException | SQLException e) {
            rollback(conn);
            throw new DataAccessException(e);
        } finally {
            restoreAutoCommit(conn);
            TransactionSynchronizationManager.unbindResource(dataSource); // 스레드로컬과 커넥션을 떼어주는 작업
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }

    private static void restoreAutoCommit(Connection conn) {
        try {
            conn.setAutoCommit(true);
        } catch (final SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private void rollback(Connection conn) {
        try {
            conn.rollback();
        } catch (final SQLException e) {
            throw new DataAccessException(e);
        }
    }
}
