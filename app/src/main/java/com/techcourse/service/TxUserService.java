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

    public TxUserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public User findById(final long id) {
        DataSource dataSource = DataSourceConfig.getInstance();
        Connection conn = DataSourceUtils.getConnection(dataSource);
        try {
            conn.setAutoCommit(false);

            User user = userService.findById(id);

            conn.commit();
            return user;
        } catch (Exception e) {
            rollback(conn);

            throw new DataAccessException("id를 찾던 중 예외가 발생했습니다: " + e);
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }

    @Override
    public void save(final User user) {
        DataSource dataSource = DataSourceConfig.getInstance();
        Connection conn = DataSourceUtils.getConnection(dataSource);
        try {
            conn.setAutoCommit(false);

            userService.save(user);

            conn.commit();
        } catch (Exception e) {
            rollback(conn);

            throw new DataAccessException("유저를 저장하던 중 예외가 발생했습니다: " + e);
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createdBy) {
        DataSource dataSource = DataSourceConfig.getInstance();
        Connection conn = DataSourceUtils.getConnection(dataSource);
        try {
            conn.setAutoCommit(false);

            userService.changePassword(id, newPassword, createdBy);

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
}
