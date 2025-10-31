package com.techcourse.service;

import static com.interface21.transaction.support.TransactionSynchronizationManager.unbindResource;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.techcourse.config.DataSourceConfig;
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

    public TxUserService(final UserService userService) {
        this.userService = userService;
        dataSource = DataSourceConfig.getInstance();
    }

    @Override
    public User findById(final long id) {
        return userService.findById(id);
    }

    @Override
    public void save(final User user) {
        userService.save(user);
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        Connection connection = null;
        try {
            connection = DataSourceUtils.getConnection(dataSource);
            connection.setAutoCommit(false);

            final var user = findById(id);
            user.changePassword(newPassword);

            userService.changePassword(user.getId(), newPassword, createBy);

            connection.commit();
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException rollback) {
                    log.error("Rollback failed", rollback);
                }
            }
            throw new DataAccessException(e);
        } catch (DataAccessException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException rollback) {
                    log.error("Rollback failed", rollback);
                }
            }
            throw e;
        } finally {
            if (connection != null) {
                DataSourceUtils.releaseConnection(connection, dataSource);
                unbindResource(dataSource);
            }
        }
    }
}
