package org.springframework.transaction.support;

import org.springframework.jdbc.CannotGetJdbcConnectionException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public abstract class TransactionSynchronizationManager {

    private static final ThreadLocal<Map<DataSource, Connection>> resources = new ThreadLocal<>();

    private TransactionSynchronizationManager() {}

    public static Connection getConnection(DataSource dataSource) {
        Map<DataSource, Connection> dataSourceAndConnection = resources.get();
        if (dataSourceAndConnection == null) {
            throw new CannotGetJdbcConnectionException("DataSource에 binding된 Connection이 없습니다.");
        }

        return dataSourceAndConnection.get(dataSource);
    }

    public static void bindConnection(DataSource dataSource, Connection connection) {
        if (resources.get() == null) {
            Map<DataSource, Connection> threadResource = new HashMap<>();
            resources.set(threadResource);
        }

        resources.get().put(dataSource, connection);
    }

    public static Connection unbindConnection(DataSource dataSource) {
        if (resources.get() == null || !resources.get().containsKey(dataSource)) {
            throw new IllegalArgumentException("등록된 DataSource가 없습니다.");
        }

        Map<DataSource, Connection> resource = resources.get();
        Connection connection = resource.get(dataSource);
        resources.remove();
        return connection;
    }
}
