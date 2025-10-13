package com.interface21.jdbc.datasource;

import com.interface21.jdbc.exception.DatabaseConnectionFailException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public abstract class PlatformTransactionManager {

    private static final ThreadLocal<Map<DataSource, Connection>> connectionHolder = new ThreadLocal<>();

    public static void begin(DataSource dataSource) throws SQLException {
        try {
            Map<DataSource, Connection> map = connectionHolder.get();
            if (map == null) {
                map = new HashMap<>();
                connectionHolder.set(map);
            }

            if (map.containsKey(dataSource)) {
                return;
            }

            Connection connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            map.put(dataSource, connection);

        } catch (SQLException e) {
            removeResource(dataSource);
            throw new DatabaseConnectionFailException("트랜잭션 시작에 실패하였습니다", e.getMessage());
        }
    }

    public static void commit(DataSource dataSource) {
        Connection connection = getBoundConnection(dataSource);
        try {
            connection.commit();
        } catch (SQLException e) {
            throw new DatabaseConnectionFailException("커밋에 실패하였습니다", e.getMessage());
        }
    }

    public static void rollback(DataSource dataSource) {
        Connection connection = getBoundConnection(dataSource);
        try {
            connection.rollback();
        } catch (SQLException e) {
            throw new DatabaseConnectionFailException("롤백에 실패하였습니다", e.getMessage());
        }
    }

    public static void end(DataSource dataSource) {
        Connection connection = getBoundConnection(dataSource);
        try {
            connection.close();
        } catch (SQLException e) {
            throw new DatabaseConnectionFailException("Connection 종료에 실패하였습니다", e.getMessage());
        } finally {
            removeResource(dataSource);
        }
    }

    public static Connection getConnection(DataSource dataSource) throws SQLException {
        Map<DataSource, Connection> map = connectionHolder.get();
        if (map != null && map.containsKey(dataSource)) {
            return map.get(dataSource);
        }
        return dataSource.getConnection();
    }

    private static Connection getBoundConnection(DataSource dataSource) {
        Map<DataSource, Connection> map = connectionHolder.get();
        if (map == null || !map.containsKey(dataSource)) {
            throw new DatabaseConnectionFailException("해당 DataSource에 대한 트랜잭션이 없습니다");
        }
        return map.get(dataSource);
    }

    private static void removeResource(DataSource dataSource) {
        Map<DataSource, Connection> map = connectionHolder.get();
        if (map != null) {
            map.remove(dataSource);
            if (map.isEmpty()) {
                connectionHolder.remove();
            }
        }
    }
}
