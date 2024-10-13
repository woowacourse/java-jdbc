package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.sql.DataSource;

public class TransactionManager {

    private final ConnectionManager connectionManager;

    public TransactionManager(final DataSource dataSource) {
        this.connectionManager = new ConnectionManager(dataSource);
    }

    public void manage(final Consumer<Connection> businessLogic) {
        connectionManager.manage(conn -> {
            execute(businessLogic, conn);
        });
    }

    private void execute(final Consumer<Connection> businessLogic, final Connection conn) {
        try {
            conn.setAutoCommit(false);
            businessLogic.accept(conn);
            conn.commit();
        } catch (SQLException e) {
            rollback(conn);
            throw new DataAccessException(e);
        }
    }

    public <T> T manage(final Function<Connection, T> businessLogic) {
        return connectionManager.manage(conn -> {
            return execute(businessLogic, conn);
        });
    }

    private <T> T execute(final Function<Connection, T> businessLogic, final Connection conn) {
        try {
            conn.setAutoCommit(false);
            T result = businessLogic.apply(conn);
            conn.commit();
            return result;
        } catch (SQLException e) {
            rollback(conn);
            throw new DataAccessException(e);
        }
    }

    private void rollback(final Connection conn) {
        try {
            conn.rollback();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }
}
