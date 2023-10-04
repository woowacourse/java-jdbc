package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueryTemplate {

    private static final Logger log = LoggerFactory.getLogger(QueryTemplate.class);

    private final DataSource dataSource;

    public QueryTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T service(String sql, QueryCallback<T> callback, Object... args) {
        try (
            Connection conn = dataSource.getConnection();
            PreparedStatement prepareStatement = setUpPreparedStatement(conn, sql, args);
        ) {
            return callback.execute(prepareStatement);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private PreparedStatement setUpPreparedStatement(Connection conn, String sql, Object... args) throws SQLException {
        PreparedStatement prepareStatement = conn.prepareStatement(sql);
        for (int i = 0; i < args.length; i++) {
            prepareStatement.setObject(i + 1, args[i]);
        }
        return prepareStatement;
    }
}
