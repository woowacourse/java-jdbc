package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import java.sql.Connection;
import java.sql.SQLException;
import org.springframework.jdbc.core.error.SqlExceptionConverter;
import org.springframework.jdbc.datasource.DataSourceUtils;

public class TransactionUserService extends TransactionService<UserService> implements UserService {

    public TransactionUserService(final UserDao userDao, final UserHistoryDao userHistoryDao) {
        super(new AppUserService(userDao, userHistoryDao));
    }

    public User findById(final long id) {
        final Connection connection = DataSourceUtils.getConnection(DataSourceConfig.getInstance());
        final User user = appService.findById(id);
        DataSourceUtils.releaseConnection(connection, DataSourceConfig.getInstance());
        return user;
    }

    public void insert(final User user) {
        final Connection connection = DataSourceUtils.getConnection(DataSourceConfig.getInstance());
        try {
            connection.setAutoCommit(false);
            appService.insert(user);
            connection.commit();
        } catch (SQLException e) {
            rollback(connection);
            throw SqlExceptionConverter.convert(e);
        } catch (Exception e) {
            rollback(connection);
            throw e;
        } finally {
            DataSourceUtils.releaseConnection(connection, DataSourceConfig.getInstance());
        }
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        final Connection connection = DataSourceUtils.getConnection(DataSourceConfig.getInstance());
        try {
            connection.setAutoCommit(false);
            appService.changePassword(id, newPassword, createBy);
            connection.commit();
        } catch (SQLException e) {
            rollback(connection);
            throw SqlExceptionConverter.convert(e);
        } catch (Exception e) {
            rollback(connection);
            throw e;
        } finally {
            DataSourceUtils.releaseConnection(connection, DataSourceConfig.getInstance());
        }
    }
}
