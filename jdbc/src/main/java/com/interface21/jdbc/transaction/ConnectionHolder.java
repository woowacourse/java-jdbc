package com.interface21.jdbc.transaction;

import java.sql.Connection;

public class ConnectionHolder {

    private ConnectionHolder() {
    }

    public static Connection connection;

    public static Connection getConnection() {
        return connection;
    }

    public static void setConnection(Connection connection) {
        System.out.println("커넥션 세팅 ! ======");
        ConnectionHolder.connection = connection;
    }
}
