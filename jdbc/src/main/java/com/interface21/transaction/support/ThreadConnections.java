package com.interface21.transaction.support;

import java.sql.Connection;
import java.util.ArrayDeque;
import java.util.Deque;


public class ThreadConnections {

    private final ThreadLocal<Deque<Connection>> connections;

    public ThreadConnections() {
        this.connections = ThreadLocal.withInitial(ArrayDeque::new);
    }

    public boolean isTransactionActive() {
        return !getConnections().isEmpty();
    }

    public Deque<Connection> getConnections() {
        return connections.get();
    }

    public Connection popConnection() {
        return getConnections().pop();
    }

    public void pushConnection(Connection connection) {
        getConnections().add(connection);
    }

    public int size() {
        return getConnections().size();
    }
}
