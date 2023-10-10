package com.techcourse.service;

import com.techcourse.dao.UserDaoWithTransaction;
import com.techcourse.dao.UserHistoryDaoWithTransaction;
import com.techcourse.domain.UserHistory;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class UserServiceWithTransaction extends UserService {

    private final DataSource dataSource;
    private final UserDaoWithTransaction userDao;
    private final UserHistoryDaoWithTransaction userHistoryDao;

    public UserServiceWithTransaction(final UserDaoWithTransaction userDao,
                                      final UserHistoryDaoWithTransaction userHistoryDao,
                                      final DataSource dataSource) {
        super(userDao, userHistoryDao);
        this.dataSource = dataSource;
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
    }

    public void changePasswordWithTransaction(final long id, final String newPassword, final String createBy) {
        final var connection = DataSourceUtils.getConnection(dataSource);
        try {
            connection.setAutoCommit(false);
            final var user = findById(id);
            user.changePassword(newPassword);
            userDao.update(connection, user);
            userHistoryDao.log(connection, new UserHistory(user, createBy));
            connection.commit();
        } catch (Exception e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                throw new DataAccessException(ex);
            }
            throw new DataAccessException(e);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }
}
