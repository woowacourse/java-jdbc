package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Consumer;
import java.util.function.Function;

public class TransactionManager {

    private final ConnectionManager connectionManager;

    public TransactionManager() {
        this.connectionManager = new ConnectionManager();
    }

    public void manage(Consumer<Connection> businessLogic) {
        connectionManager.manage(conn -> {
            try {
                conn.setAutoCommit(false);
                businessLogic.accept(conn);
                conn.commit();
            } catch (SQLException e) {
                rollback(conn);
                throw new DataAccessException(e);
            }
        });
    }

    public <T> T manage(Function<Connection, T> businessLogic) {
        return connectionManager.manage(conn -> {
            try {
                conn.setAutoCommit(false);
                T result = businessLogic.apply(conn);
                conn.commit();
                return result;
            } catch (SQLException e) {
                rollback(conn);
                throw new DataAccessException(e);
            }
        });
    }

    private void rollback(Connection conn) {
        try {
            conn.rollback();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }
}
