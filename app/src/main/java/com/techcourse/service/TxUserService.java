package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.domain.User;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public class TxUserService implements UserService {

  private final DataSource dataSource;
  private final UserService userService;

  public TxUserService(final DataSource dataSource, final UserService userService) {
    this.dataSource = dataSource;
    this.userService = userService;
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
  public void changePassword(final long id, final String newPassword, final String createdBy) {
    if (TransactionSynchronizationManager.getResource(dataSource) != null) {
      throw new IllegalStateException("A transaction is already active for this DataSource");
    }
    final Connection connection = DataSourceUtils.getConnection(dataSource);
    Throwable originalException = null;
    try {
      TransactionSynchronizationManager.bindResource(dataSource, connection);
      connection.setAutoCommit(false);
      userService.changePassword(id, newPassword, createdBy);
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
