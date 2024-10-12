package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Supplier;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TxUserService implements UserService {

    private static final Logger log = LoggerFactory.getLogger(TxUserService.class);

    private final UserService userService;

    public TxUserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public User findById(final long id) {
        return userService.findById(id);
    }

    @Override
    public User findByAccount(final String account) {
        return userService.findByAccount(account);
    }

    @Override
    public void insert(final User user) {
        userService.insert(user);
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        executeInTransaction(() -> {
            userService.changePassword(id, newPassword, createBy);
            return null;
        });
    }

    private <T> T executeInTransaction(Supplier<T> callback) {
        DataSource dataSource = DataSourceConfig.getInstance();
        Connection conn = DataSourceUtils.getConnection(dataSource);
        try {
            conn.setAutoCommit(false);
            return execute(callback, conn);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }

    private <T> T execute(Supplier<T> callback, Connection conn) throws SQLException {
        try {
            T result = callback.get();
            conn.commit();
            return result;
        } catch (SQLException | DataAccessException e) {
            try {
                conn.rollback();
            } catch (SQLException ignored) {
            }
            throw new DataAccessException(e);
        }
    }
}
