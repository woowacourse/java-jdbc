package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.ConnectionAgent;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);
    private static final int FIRST_RESULT = 0;
    private static final String LOG_FORMAT = "query : {}";

    private final ConnectionAgent connectionAgent;

    public JdbcTemplate(final DataSource dataSource) {
        this.connectionAgent = new ConnectionAgent(dataSource);
    }

    public void update(final String sql, final Object... params) {
        execute(sql, PreparedStatement::executeUpdate, params);
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... params) {
        return execute(sql, statement -> getResults(rowMapper, statement), params);
    }

    public <T> Optional<T> queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... params) {
        final List<T> results = execute(sql, statement -> getResults(rowMapper, statement), params);

        if (results.size() > 1) {
            throw new DataAccessException("row 갯수가 1보다 많아요.");
        }

        if (results.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(results.get(FIRST_RESULT));
    }

    private <T> T execute(final String sql,
                          final StatementExecutor<T> statementExecutor,
                          final Object... params
    ) {
        final Connection conn = connectionAgent.getConnection();

        try (final PreparedStatement pstmt = PreparedStatementGenerator.generate(conn, sql, params)) {
            log.debug(LOG_FORMAT, sql);
            return statementExecutor.execute(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private <T> List<T> getResults(final RowMapper<T> rowMapper, final PreparedStatement pstmt) throws SQLException {
        try (final ResultSet rs = pstmt.executeQuery()) {
            final List<T> results = new ArrayList<>();
            while (rs.next()) {
                results.add(rowMapper.mapRow(rs));
            }
            return results;
        }
    }
}
