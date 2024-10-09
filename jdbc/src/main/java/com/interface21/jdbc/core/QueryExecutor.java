package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueryExecutor {

    private static final Logger log = LoggerFactory.getLogger(QueryExecutor.class);

    private final DataSource dataSource;

    public QueryExecutor(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    <T> T executeFunction(QueryFunction<PreparedStatement, T> function, String sql, Object... args) {
        try (Connection conn = this.dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            this.setParameters(pstmt, args);
            return function.apply(pstmt);
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    void executeConsumer(QueryConsumer<PreparedStatement> consumer, String sql, Object... args) {
        try (Connection conn = this.dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            this.setParameters(pstmt, args);
            consumer.accept(pstmt);
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    private void setParameters(PreparedStatement pstmt, Object... args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            pstmt.setObject(i + 1, args[i]);
        }
    }
}
