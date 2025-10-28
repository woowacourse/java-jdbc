package com.interface21.jdbc.datasource;

import com.interface21.jdbc.exception.DatabaseConnectionFailException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public abstract class PlatformTransactionManager {

    public static void begin(DataSource dataSource) throws SQLException {
        try {
            Connection connection = DataSourceUtils.getConnection(dataSource);
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            throw new DatabaseConnectionFailException("트랜잭션 시작에 실패하였습니다", e.getMessage());
        }
    }

    public static void commit(DataSource dataSource) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            connection.commit();
        } catch (SQLException e) {
            throw new DatabaseConnectionFailException("커밋에 실패하였습니다", e.getMessage());
        }
    }

    public static void rollback(DataSource dataSource) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            connection.rollback();
        } catch (SQLException e) {
            throw new DatabaseConnectionFailException("롤백에 실패하였습니다", e.getMessage());
        }
    }

    public static void end(DataSource dataSource) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            connection.close();
        } catch (SQLException e) {
            throw new DatabaseConnectionFailException("Connection 종료에 실패하였습니다", e.getMessage());
        } finally {
            DataSourceUtils.releaseConnection(dataSource);
        }
    }
}
