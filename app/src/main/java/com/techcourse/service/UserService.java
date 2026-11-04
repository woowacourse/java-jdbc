package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public class UserService {

  private final DataSource dataSource;
  private final UserDao userDao;
  private final UserHistoryDao userHistoryDao;

  public UserService(
      final DataSource dataSource, final UserDao userDao, final UserHistoryDao userHistoryDao) {
    this.dataSource = dataSource;
    this.userDao = userDao;
    this.userHistoryDao = userHistoryDao;
  }

  public User findById(final long id) {
    return userDao.findById(id);
  }

  public void insert(final User user) {
    userDao.insert(user);
  }

  public void changePassword(final long id, final String newPassword, final String createdBy) {
    if (TransactionSynchronizationManager.getResource(dataSource) != null) {
      throw new IllegalStateException("A transaction is already active for this DataSource");
    }
    final Connection connection = DataSourceUtils.getConnection(dataSource);
    Throwable originalException = null;
    try {
      TransactionSynchronizationManager.bindResource(dataSource, connection);
      connection.setAutoCommit(false);
      final User user = userDao.findById(id);
      user.changePassword(newPassword);
      userDao.update(user);
      userHistoryDao.log(new UserHistory(user, createdBy));
      connection.commit();
    } catch (SQLException exception) {
      final DataAccessException dataAccessException =
          new DataAccessException("Failed to complete password change transaction", exception);
      originalException = dataAccessException;
      rollback(connection, dataAccessException);
      throw dataAccessException;
    } catch (RuntimeException | Error exception) {
      originalException = exception;
      rollback(connection, exception);
      throw exception;
    } finally {
      TransactionSynchronizationManager.unbindResource(dataSource);
      DataSourceUtils.releaseConnection(connection, dataSource, originalException);
    }
  }

  private void rollback(final Connection connection, final Throwable originalException) {
    try {
      connection.rollback();
    } catch (SQLException rollbackException) {
      originalException.addSuppressed(rollbackException);
    }
  }
}
