package com.interface21.jdbc.datasource;

import com.interface21.jdbc.exception.JdbcFailException;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public class LocalTransactionManager {

    private static final ThreadLocal<Connection> connectionHolder = new ThreadLocal<>();

    public static void begin(DataSource dataSource) throws SQLException {
        try{
            Connection connection = dataSource.getConnection();
            connectionHolder.set(connection);
            connection.setAutoCommit(false);
        }catch (SQLException e ){
            throw new JdbcFailException("연결에 실패하였습니다");
        }
    }

    public static void end() {
        try{
            connectionHolder.get().close();
            connectionHolder.remove();
        }catch (SQLException e ){
            throw new JdbcFailException("종료에 실패하였습니다");
        }
    }

    public static void commit() {
        try{
            Connection connection = connectionHolder.get();
            connection.commit();
        }catch (SQLException e ){
            throw new JdbcFailException("연결에 실패하였습니다");
        }
    }

    public static void rollback(){
        Connection connection = connectionHolder.get();
        try {
            connection.rollback();
        } catch (SQLException e) {
            throw new JdbcFailException("롤백에 실패하였습니다");
        }
    }

    public static Connection getConnection(DataSource ds) throws SQLException {
        Connection conn = connectionHolder.get();
        if (conn != null){
            return conn;
        }
        return ds.getConnection();
    }
}

