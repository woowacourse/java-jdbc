package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(String sql, Object... args) {
        executePreparedStatement(sql, PreparedStatement::executeUpdate, args);
    }

    private void bind(PreparedStatement pstmt, Object... args) throws SQLException {
        int index = 1;
        for (Object arg : args) {
            pstmt.setObject(index++, arg);
        }
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        List<T> results = executePreparedStatement(
                sql,
                pstmt -> mapResults(rowMapper, pstmt),
                args
        );
        return results.isEmpty() ? null : results.get(0);
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) {
        return executePreparedStatement(
                sql,
                pstmt -> mapResults(rowMapper, pstmt),
                args
        );
    }

    private <T> List<T> mapResults(RowMapper<T> rowMapper, PreparedStatement pstmt) throws SQLException {
        ResultSet rs = pstmt.executeQuery();
        List<T> results = new ArrayList<>();
        while (rs.next()) {
            results.add(rowMapper.mapRow(rs));
        }
        return results;
    }

    private <T> T executePreparedStatement(
            final String sql,
            final PreparedStatementCallback<T> preparedStatementCallback,
            final Object... args
    ) {
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            bind(pstmt, args);
            return preparedStatementCallback.callback(pstmt);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

