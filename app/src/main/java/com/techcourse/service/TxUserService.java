package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.SQLExceptionTranslator;

public class TxUserService implements UserService {

    private final DataSource dataSource;
    private final UserService userService;

    public TxUserService(UserService userService) {
        this.dataSource = DataSourceConfig.getInstance();
        this.userService = userService;
    }

    @Override
    public User findById(long id) {
        TransactionCallback<User> action = () -> userService.findById(id);
        return doTransactionWithReturn(action);
    }

    @Override
    public void insert(User user) {
        TransactionCallback<Void> action = () -> {
            userService.insert(user);
            return null;
        };
        doTransactionWithoutReturn(action);
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        TransactionCallback<Void> action = () -> {
            userService.changePassword(id, newPassword, createBy);
            return null;
        };
        doTransactionWithoutReturn(action);
    }

    private <T> T doTransactionWithReturn(TransactionCallback<T> action) {
        Connection conn = DataSourceUtils.getConnection(dataSource);
        T result;
        try {
            try {
                conn.setAutoCommit(false);
                result = action.doInTransaction();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw SQLExceptionTranslator.translate("", e);
        } finally {
            DataSourceUtils.releaseConnection(dataSource);
        }
        return result;
    }

    private <T> void doTransactionWithoutReturn(TransactionCallback<T> action) {
        Connection conn = DataSourceUtils.getConnection(dataSource);
        try {
            try {
                conn.setAutoCommit(false);
                action.doInTransaction();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw SQLExceptionTranslator.translate("", e);
        } finally {
            DataSourceUtils.releaseConnection(dataSource);
        }
    }
}

