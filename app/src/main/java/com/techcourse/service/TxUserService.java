package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Supplier;
import javax.sql.DataSource;

public class TxUserService implements UserService {

    private final UserService userService;

    public TxUserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public User findById(final long id) {
        return executeOnTransaction(() -> userService.findById(id));
    }

    @Override
    public void save(final User user) {
        executeOnTransaction(() -> userService.save(user));
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createdBy) {
        executeOnTransaction(() -> userService.changePassword(id, newPassword, createdBy));
    }

    private void executeOnTransaction(Runnable runnable) {
        DataSource dataSource = DataSourceConfig.getInstance();
        Connection conn = DataSourceUtils.getConnection(dataSource);
        try {
            conn.setAutoCommit(false);

            runnable.run();

            conn.commit();
        } catch (Exception e) {
            rollback(conn);

            throw new DataAccessException("비밀번호를 수정하던 중 예외가 발생했습니다: " + e);
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }

    private void rollback(Connection conn) {
        try {
            conn.rollback();
        } catch (SQLException e) {
            throw new DataAccessException("롤백을 진행하던 중 예외가 발생했습니다: " + e);
        }
    }

    private <T> T executeOnTransaction(Supplier<T> supplier) {
        DataSource dataSource = DataSourceConfig.getInstance();
        Connection conn = DataSourceUtils.getConnection(dataSource);
        try {
            conn.setAutoCommit(false);

            T t = supplier.get();

            conn.commit();
            return t;
        } catch (Exception e) {
            rollback(conn);

            throw new DataAccessException("비밀번호를 수정하던 중 예외가 발생했습니다: " + e);
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }
}
