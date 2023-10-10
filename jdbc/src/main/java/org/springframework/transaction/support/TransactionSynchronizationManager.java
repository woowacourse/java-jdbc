package org.springframework.transaction.support;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;

public class TransactionSynchronizationManager {

  private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();
  private static final ThreadLocal<Boolean> actualTransactionActive = new ThreadLocal<>();

  private TransactionSynchronizationManager() {
  }

  public static void start() {
    actualTransactionActive.set(Boolean.TRUE);
  }

  private static Connection getConnection(final DataSource dataSource) {
    final Map<DataSource, Connection> resource = getResources();

    final Connection connection = resource.get(dataSource);

    if (connection == null) {
      return null;
    }

    if (isTransactionActive()) {
      begin(connection);
    }

    return connection;
  }

  public static Optional<Connection> getResource(final DataSource dataSource) {
    if (getResources() == null) {
      return Optional.empty();
    }

    return Optional.ofNullable(getConnection(dataSource));
  }

  public static void bindResource(final DataSource dataSource, final Connection connection) {
    if (getResources() == null) {
      resources.set(new HashMap<>());
    }

    getResources()
        .put(dataSource, connection);
  }

  private static void begin(final Connection connection) {
    try {
      connection.setAutoCommit(false);
    } catch (SQLException e) {
      throw new DataAccessException(e);
    }
  }

  private static boolean isTransactionActive() {
    final Boolean isActive = actualTransactionActive.get();
    return isActive != null && isActive;
  }

  public static void commit(final DataSource dataSource) {
    final Connection connection = getResources().get(dataSource);

    try {
      connection.commit();
    } catch (SQLException e) {
      throw new DataAccessException(e);
    }
  }

  public static void rollback(final DataSource dataSource) {
    final Connection connection = getResources().get(dataSource);

    try {
      if (isTransactionActive()) {
        connection.rollback();
      }
    } catch (SQLException e) {
      throw new DataAccessException(e);
    }
  }

  public static void release(final DataSource dataSource) {
    final Connection connection = getResources().get(dataSource);

    if (connection == null) {
      throw new DataAccessException("not exist connection");
    }

    DataSourceUtils.releaseConnection(connection);
    actualTransactionActive.remove();
    getResources().remove(dataSource);
  }

  private static Map<DataSource, Connection> getResources() {
    return resources.get();
  }
}
