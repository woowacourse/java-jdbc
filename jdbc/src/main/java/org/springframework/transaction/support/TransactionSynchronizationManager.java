package org.springframework.transaction.support;

import org.springframework.dao.DataAccessException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {
    }

    public static Connection getResource(DataSource key) {
        if (resources.get() == null) {
            try {
                Map<DataSource, Connection> threadResource = new HashMap<>();
                threadResource.put(key, key.getConnection());
                resources.set(threadResource);
            } catch (SQLException e) {
                throw new DataAccessException("cannot get Connection", e);
            }
        }

        return resources.get().get(key);
    }

    public static void bindResource(DataSource key, Connection value) {
    }

    public static Connection unbindResource(DataSource key) {
        try {
            Map<DataSource, Connection> resource = resources.get();
            Connection connection = resource.get(key);
            connection.close();
            resources.remove();
            return connection;
        } catch (Exception e) {
            throw new DataAccessException("connection close failed", e);
        }
    }

    public static void execute(DataSource dataSource, Runnable runnable) {
        Connection connection = null;
        try {
            connection = getResource(dataSource);
            connection.setAutoCommit(false);

            runnable.run();

            connection.commit();
        } catch (Exception e) {
            ConnectionManager.rollback(e, connection);
        } finally {
            ConnectionManager.close(connection);
        }
    }
}
