package org.springframework.transaction.support;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;

public class TransactionSynchronizationManager {

  private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();
  private static final ThreadLocal<Boolean> actualTransactionActive = new ThreadLocal<>();

  private TransactionSynchronizationManager() {
  }

  public static void start() {
    actualTransactionActive.set(Boolean.TRUE);
  }

  public static Connection getConnection(final DataSource dataSource) {
    if (resources.get() == null) {
      resources.set(new HashMap<>());
    }

    final Map<DataSource, Connection> resource = resources.get();

    final Connection connection = resource.getOrDefault(
        dataSource,
        createConnectionFrom(dataSource)
    );

    if (isTransactionActive()) {
      setAutoCommit(connection);
    }

    return connection;
  }

  private static Connection createConnectionFrom(final DataSource dataSource) {
    try {
      return resources.get()
          .put(dataSource, dataSource.getConnection());
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }


  private static void setAutoCommit(final Connection connection) {
    try {
      connection.setAutoCommit(false);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  private static boolean isTransactionActive() {
    final Boolean isActive = actualTransactionActive.get();
    return isActive != null && isActive;
  }

  public static void commit(final DataSource dataSource) {
    final Connection connection = resources.get().get(dataSource);

    try {
      connection.commit();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public static void rollback(final DataSource dataSource) {
    final Connection connection = resources.get().get(dataSource);

    try {
      if (isTransactionActive()) {
        connection.rollback();
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public static void release(final DataSource dataSource) {
    final Connection connection = resources.get().get(dataSource);

    try {
      connection.close();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    } finally {
      actualTransactionActive.remove();
      resources.remove();
    }
  }
}
