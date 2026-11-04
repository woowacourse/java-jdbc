package com.interface21.jdbc.datasource;

import com.interface21.jdbc.CannotGetJdbcConnectionException;
import com.interface21.transaction.support.TransactionSynchronizationManager;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public abstract class DataSourceUtils {

  private DataSourceUtils() {}

  public static Connection getConnection(final DataSource dataSource) {
    final Connection connection = TransactionSynchronizationManager.getResource(dataSource);
    if (connection != null) {
      return connection;
    }

    try {
      return dataSource.getConnection();
    } catch (SQLException exception) {
      throw new CannotGetJdbcConnectionException("Failed to obtain JDBC Connection", exception);
    }
  }

  public static void releaseConnection(final Connection connection, final DataSource dataSource) {
    if (connection == null
        || connection == TransactionSynchronizationManager.getResource(dataSource)) {
      return;
    }
    try {
      connection.close();
    } catch (SQLException exception) {
      throw new CannotGetJdbcConnectionException("Failed to close JDBC Connection", exception);
    }
  }

  /** 작업이 이미 실패했다면 반환 실패를 원래 예외에 첨부한다. */
  public static void releaseConnection(
      final Connection connection, final DataSource dataSource, final Throwable originalException) {
    try {
      releaseConnection(connection, dataSource);
    } catch (RuntimeException | Error releaseException) {
      if (originalException == null) {
        throw releaseException;
      }
      originalException.addSuppressed(releaseException);
    }
  }
}
