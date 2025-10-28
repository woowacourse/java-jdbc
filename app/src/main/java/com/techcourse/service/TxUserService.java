package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.PlatformTransactionManager;
import com.techcourse.domain.User;

import java.sql.SQLException;
import javax.sql.DataSource;

public class TxUserService implements UserService {

    private final UserService userService;
    private final DataSource dataSource;

    public TxUserService(UserService userService,DataSource dataSource) {
        this.dataSource = dataSource;
        this.userService = userService;
    }

    @Override
    public User findById(final long id) {
        return transactionWithResult(() -> userService.findById(id));
    }

    @Override
    public void save(User user) {
        transactionWithoutResult(() -> userService.save(user));
    }

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        transactionWithoutResult(() -> userService.changePassword(id,newPassword,createBy));
    }

    private <T> T transactionWithResult(TransactionCallback<T> callback){
        try{
            PlatformTransactionManager.begin(dataSource);
            return callback.doInTransaction();
        } catch (DataAccessException e) {
            PlatformTransactionManager.rollback(dataSource);
            throw e;
        } catch (SQLException e) {
            PlatformTransactionManager.rollback(dataSource);
            throw new DataAccessException(e);
        } finally {
            PlatformTransactionManager.end(dataSource);
        }
    }

    private void transactionWithoutResult(TransactionCallbackWithoutResult callback){
        try{
            PlatformTransactionManager.begin(dataSource);
            callback.doInTransaction();
        } catch (DataAccessException e) {
            PlatformTransactionManager.rollback(dataSource);
            throw e;
        } catch (SQLException e) {
            PlatformTransactionManager.rollback(dataSource);
            throw new DataAccessException(e);
        } finally {
            PlatformTransactionManager.end(dataSource);
        }
    }

    @FunctionalInterface
    interface TransactionCallback<T> {
        T doInTransaction() throws SQLException;
    }

    @FunctionalInterface
    interface TransactionCallbackWithoutResult {
        void doInTransaction() throws SQLException;
    }
}
