package org.springframework.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(final String sql, final Object... objects) {
        executePreparedStatement(sql, PreparedStatement::executeUpdate, objects);
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... objects) {
        return executePreparedStatement(sql, preparedStatement -> {
            final ResultSet rs = preparedStatement.executeQuery();
            return extractSingleResult(rowMapper, rs);
        }, objects);
    }

    public <T> List<T> queryForObjects(final String sql, final RowMapper<T> rowMapper, final Object... objects) {
        return executePreparedStatement(sql, preparedStatement -> {
            final ResultSet rs = preparedStatement.executeQuery();
            return extractListResult(rowMapper, rs);
        },objects);
    }

    private static <T> T extractSingleResult(final RowMapper<T> rowMapper, final ResultSet rs) throws SQLException {
        if (rs.next()) {
            return rowMapper.execute(rs);
        }
        return null;
    }

    private static <T> List<T> extractListResult(final RowMapper<T> rowMapper, final ResultSet rs) throws SQLException {
        final List<T> results = new ArrayList<>();
        while (rs.next()) {
            results.add(rowMapper.execute(rs));
        }
        return results;
    }

    private <T> T executePreparedStatement(final String sql, PreparedStatementExecutor<T> preparedStatementExecutor,
                                           final Object... objects) {
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement pstmt = conn.prepareStatement(sql)) {

            log.debug("query : {}", sql);
            for (int i = 0; i < objects.length; i++) {
                pstmt.setObject(i + 1, objects[i]);
            }

            return preparedStatementExecutor.execute(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }
}
