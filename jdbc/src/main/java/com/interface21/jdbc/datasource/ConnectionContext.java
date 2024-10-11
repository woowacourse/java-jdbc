package com.interface21.jdbc.datasource;

public class ConnectionContext {
    public static ThreadLocal<Connection> conn = new ThreadLocal<>();
}
