package com.techcourse.service;

import com.interface21.context.stereotype.Component;
import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.domain.User;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import javax.sql.DataSource;

@Component
public class TxUserService implements UserService {

    private final UserService userService;  // AppUserService를 감싸서 사용
    private final DataSource dataSource;

    public TxUserService(UserService userService, DataSource dataSource) {
        this.userService = userService;
        this.dataSource = dataSource;
    }

    @Override
    public User findById(long id) {
        return userService.findById(id);
    }

    @Override
    public Optional<User> findByAccount(String account) {
        return userService.findByAccount(account);
    }

    @Override
    public void save(User user) {
        userService.save(user);
    }

    @Override
    public void changePassword(long id, String newPassword, String createdBy) {
        Connection conn = DataSourceUtils.getConnection(dataSource);
        boolean isNewTransaction = false;

        try {
            if (!TransactionSynchronizationManager.hasResource(dataSource)) {
                conn.setAutoCommit(false);
                TransactionSynchronizationManager.bindResource(dataSource, conn);
                isNewTransaction = true;
            }
            userService.changePassword(id, newPassword, createdBy);
            if (isNewTransaction) {
                conn.commit();
            }
        } catch (Exception e) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                throw new DataAccessException(ex);
            }
            throw new DataAccessException(e);
        } finally {
            if (isNewTransaction) {
                TransactionSynchronizationManager.unbindResource(dataSource);
                DataSourceUtils.releaseConnection(conn, dataSource);
            }
        }
    }
}
