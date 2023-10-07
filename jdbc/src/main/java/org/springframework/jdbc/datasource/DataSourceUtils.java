package org.springframework.jdbc.datasource;

import java.util.function.Function;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

// 4단계 미션에서 사용할 것
public abstract class DataSourceUtils {

  private DataSourceUtils() {
  }

  public static Connection getConnection(DataSource dataSource) {
    return TransactionSynchronizationManager.getResource(dataSource)
        .orElseGet(() -> createConnection(dataSource));
  }

  private static Connection createConnection(final DataSource dataSource) {
    try {
      final Connection connection = dataSource.getConnection();
      TransactionSynchronizationManager.bindResource(dataSource, connection);
      return connection;
    } catch (SQLException ex) {
      throw new CannotGetJdbcConnectionException("Failed to obtain JDBC Connection", ex);
    }
  }

  public static void releaseConnection(Connection connection) {
    try {
      connection.close();
    } catch (SQLException ex) {
      throw new CannotGetJdbcConnectionException("Failed to close JDBC Connection");
    }
  }
}
