package com.techcourse.service;

import com.techcourse.domain.User;
import nextstep.jdbc.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.sql.SQLException;

public class TxUserService implements UserService {

    private final DataSource dataSource;
    private final UserService userService;

    public TxUserService(final DataSource dataSource, final UserService userService) {
        this.dataSource = dataSource;
        this.userService = userService;
    }

    @Override
    public User findById(final long id) {
        return userService.findById(id);
    }

    @Override
    public void insert(final User user) {
        userService.insert(user);
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        /* ===== 트랜잭션 영역(low level) ===== */
        TransactionSynchronizationManager.initSynchronization();
        final var connection = DataSourceUtils.getConnection(dataSource);
        try (connection) {
            connection.setAutoCommit(false);
        /* ===== 트랜잭션 영역(low level) ===== */


        /* ===== 비즈니스 로직 영역 ===== */
            userService.changePassword(id, newPassword, createBy);
        /* ===== 비즈니스 로직 영역 ===== */


        /* ===== 트랜잭션 영역(low level) ===== */
            connection.commit();
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                throw new DataAccessException(ex);
            }
            throw new DataAccessException(e);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
            TransactionSynchronizationManager.unbindResource(dataSource);
            TransactionSynchronizationManager.clearSynchronization();
        }
        /* ===== 트랜잭션 영역(low level) ===== */
    }
}
