package com.interface21.transaction.support;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;

public abstract class TransactionSynchronizationManager {

  private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

  private TransactionSynchronizationManager() {}

  public static Connection getResource(final DataSource dataSource) {
    final Map<DataSource, Connection> connections = resources.get();
    if (connections == null) {
      return null;
    }
    return connections.get(dataSource);
  }

  public static void bindResource(final DataSource dataSource, final Connection connection) {
    Map<DataSource, Connection> connections = resources.get();
    if (connections == null) {
      connections = new HashMap<>();
      resources.set(connections);
    }
    if (connections.containsKey(dataSource)) {
      throw new IllegalStateException("Connection is already bound to this DataSource");
    }
    connections.put(dataSource, connection);
  }

  public static Connection unbindResource(final DataSource dataSource) {
    final Map<DataSource, Connection> connections = resources.get();
    if (connections == null) {
      return null;
    }
    final Connection connection = connections.remove(dataSource);
    if (connections.isEmpty()) {
      resources.remove();
    }
    return connection;
  }
}
