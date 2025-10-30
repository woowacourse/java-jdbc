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

    public TxUserService(final UserService userService) {
        this.userService = userService;
        this.dataSource = DataSourceConfig.getInstance();
    }

    @Override
    public User findById(final long id) {
        Connection conn = DataSourceUtils.getConnection(dataSource);

        try {
            conn.setReadOnly(true);
            return userService.findById(id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void save(final User user) {
        Connection conn = DataSourceUtils.getConnection(dataSource);

        try {
            conn.setAutoCommit(false);

            try {
                userService.save(user);

                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                DataSourceUtils.releaseConnection(conn, dataSource);
                TransactionSynchronizationManager.unbindResource(dataSource, conn);
            }
        } catch (DataAccessException | SQLException e) {
            throw new DataAccessException("User 정보 저장 실패", e);
        }
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        Connection conn = DataSourceUtils.getConnection(dataSource);
        try {
            conn.setAutoCommit(false);

            try {
                userService.changePassword(id, newPassword, createBy);

                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                DataSourceUtils.releaseConnection(conn, dataSource);
                TransactionSynchronizationManager.unbindResource(dataSource, conn);
            }
        } catch (DataAccessException | SQLException e) {
            throw new DataAccessException("비밀번호 변경 실패", e);
        }
    }
}
