package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.techcourse.domain.User;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public class TxUserService implements UserService {

    private final AppUserService appUserService;
    private final DataSource dataSource;

    public TxUserService(final AppUserService appUserService, final DataSource dataSource) {
        this.appUserService = appUserService;
        this.dataSource = dataSource;
    }

    @Override
    public User findById(final long id) {
        final Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            connection.setAutoCommit(false);
            final var user = appUserService.findById(id);
            connection.commit();
            return user;
        } catch (DataAccessException e) {
            rollback(e, connection);
            throw new DataAccessException(e);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        } finally {
            DataSourceUtils.releaseConnection(dataSource);
        }
    }

    @Override
    public void insert(final User user) {
        final Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            connection.setAutoCommit(false);
            appUserService.insert(user);
            connection.commit();
        } catch (DataAccessException e) {
            rollback(e, connection);
            throw new DataAccessException(e);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        } finally {
            DataSourceUtils.releaseConnection(dataSource);
        }
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        final Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            connection.setAutoCommit(false);
            appUserService.changePassword(id, newPassword, createBy);
            connection.commit();
        } catch (DataAccessException e) {
            rollback(e, connection);
            throw new DataAccessException(e);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        } finally {
            DataSourceUtils.releaseConnection(dataSource);
        }
    }

    private void rollback(final DataAccessException e, final Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException rollbackEx) {
            e.addSuppressed(rollbackEx);
        }
    }

}
