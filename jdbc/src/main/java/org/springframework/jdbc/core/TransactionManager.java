package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public class TransactionManager {

  private static final ThreadLocal<Connection> resources = new ThreadLocal<>();
  private static final ThreadLocal<Boolean> actualTransactionActive = new ThreadLocal<>();

  private TransactionManager() {
  }

  public static void start() {
    actualTransactionActive.set(Boolean.TRUE);
  }

  public static Connection getConnection(final DataSource dataSource) {
    if (resources.get() == null) {
      createConnectionFrom(dataSource);
    }

    final Connection connection = resources.get();

    if (isTransactionActive()) {
      setAutoCommit(connection);
    }

    return connection;
  }

  private static void setAutoCommit(final Connection connection) {
    try {
      connection.setAutoCommit(false);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  private static void createConnectionFrom(final DataSource dataSource) {
    try {
      resources.set(dataSource.getConnection());
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  private static boolean isTransactionActive() {
    final Boolean isActive = actualTransactionActive.get();
    return isActive != null && isActive;
  }

  public static void commit() {
    final Connection connection = resources.get();

    try {
      connection.commit();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public static void rollback() {
    try {
      if (isTransactionActive()) {
        final Connection connection = resources.get();
        connection.rollback();
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public static void release() {
    final Connection connection = resources.get();

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
