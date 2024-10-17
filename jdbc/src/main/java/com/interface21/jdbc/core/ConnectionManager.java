package com.interface21.jdbc.core;

import com.interface21.jdbc.datasource.DataSourceUtils;
import java.sql.Connection;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.sql.DataSource;

public class ConnectionManager {

    private final DataSource dataSource;

    public ConnectionManager(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void manage(final Consumer<Connection> execution) {
        Connection conn = DataSourceUtils.getConnection(dataSource);
        try {
            execution.accept(conn);
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }

    public <T> T manage(final Function<Connection, T> execution) {
        Connection conn = DataSourceUtils.getConnection(dataSource);
        try {
            return execution.apply(conn);
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }
}
