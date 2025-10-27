package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.domain.User;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TxUserService implements UserService {

    private static final Logger log = LoggerFactory.getLogger(TxUserService.class);

    private final UserService userService;
    private final DataSource dataSource;

    public TxUserService(
            final UserService userService,
            final DataSource dataSource
    ) {
        this.userService = userService;
        this.dataSource = dataSource;
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
    public void changePassword(
            final long id,
            final String newPassword,
            final String createdBy
    ) {
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            TransactionSynchronizationManager.bindResource(dataSource, connection);
            connection.setAutoCommit(false);
            userService.changePassword(id, newPassword, createdBy);
            connection.commit();
        } catch (final Exception e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (final SQLException ex) {
                    log.error("JDBC Connection 롤백 실패", ex);
                }
            }
            throw new DataAccessException("비밀번호 변경 중 오류가 발생했습니다.", e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (final SQLException e) {
                    log.error("JDBC Connection 종료 실패", e);
                }
            }
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }
}
