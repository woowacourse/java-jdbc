package org.springframework.transaction.support;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.dao.DataAccessException;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = ThreadLocal.withInitial(HashMap::new);

    private TransactionSynchronizationManager() {}

    public static Connection getResource(DataSource key) {
        Map<DataSource, Connection> connections = resources.get();
        Connection connection = connections.get(key);
        if (connection == null) {
            throw new DataAccessException("there is no connection");
        }
        return connection;
    }

    public static void bindResource(DataSource key, Connection value) {
        Map<DataSource, Connection> connections = resources.get();
        Connection oldConnection = connections.put(key, value);
        if (oldConnection != null) {
            throw new DataAccessException("connection is already bound");
        }
    }

    public static Connection unbindResource(DataSource key) {
        Map<DataSource, Connection> connections = resources.get();
        return connections.remove(key);
    }
}
