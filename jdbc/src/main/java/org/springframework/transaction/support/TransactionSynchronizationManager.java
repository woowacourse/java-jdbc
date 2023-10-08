package org.springframework.transaction.support;

import org.springframework.jdbc.CannotGetJdbcConnectionException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {}

    public static Connection getResource(DataSource key) {
        Map<DataSource, Connection> map = getMap();
        Connection connection = map.get(key);
        try {
            if(connection == null || connection.isClosed()){
                connection = key.getConnection();
                map.put(key, connection);
            }
            return connection;
        } catch (SQLException e) {
            throw new CannotGetJdbcConnectionException("Failed to obtain JDBC Connection");
        }
    }

    private static Map<DataSource, Connection> getMap() {
        Map<DataSource, Connection> map = resources.get();
        if(map == null){
            map = new HashMap<>();
            resources.set(map);
        }
        return map;
    }

    public static void bindResource(DataSource key, Connection value) {
        Map<DataSource, Connection> map = resources.get();
        map.put(key, value);
    }

    public static void unbindResource(DataSource key) {
        Map<DataSource, Connection> map = resources.get();
        map.remove(key);
    }
}
