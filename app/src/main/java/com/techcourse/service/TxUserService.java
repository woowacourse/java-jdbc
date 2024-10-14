package com.techcourse.service;

import java.sql.Connection;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.TransactionException;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;

public class TxUserService implements UserService {

    private static final Logger log = LoggerFactory.getLogger(TxUserService.class);

    private final UserService appUserService;

    public TxUserService(UserService appUserService) {
        this.appUserService = appUserService;
    }

    @Override
    public void insert(final User user) {
        appUserService.insert(user);
    }

    @Override
    public void changePassword(long id, String newPassword, String createdBy) {
        Connection connection = DataSourceUtils.getConnection(DataSourceConfig.getInstance());
        try {
            connection.setAutoCommit(false);
            appUserService.changePassword(id, newPassword, createdBy);
            connection.commit();
        } catch (SQLException e) {
            rollback(connection);
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        } catch (Exception e) {
            rollback(connection);
            throw e;
        } finally {
            DataSourceUtils.releaseConnection(connection, DataSourceConfig.getInstance());
        }
    }

    @Override
    public User getById(long id) {
        return appUserService.getById(id);
    }

    private void rollback(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException e) {
            throw new TransactionException("Transaction rollback failed due to: ", e);
        }
    }
}
