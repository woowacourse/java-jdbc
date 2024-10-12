package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.sql.DataSource;

public class ConnectionManager {

    private final DataSource dataSource;

    public ConnectionManager(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void manage(final Consumer<Connection> execution) {
        try (Connection conn = dataSource.getConnection()) {
            execution.accept(conn);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public <T> T manage(final Function<Connection, T> execution) {
        try (Connection conn = dataSource.getConnection()) {
            return execution.apply(conn);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }
}
