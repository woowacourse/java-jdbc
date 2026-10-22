package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.dao.IncorrectResultSizeDataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = Objects.requireNonNull(dataSource, "DataSource must not be null");
    }

    public int update(final String sql, final Object... args) {
        return execute(sql, args, PreparedStatement::executeUpdate);
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return execute(sql, args, pstmt -> mapRows(pstmt, rowMapper));
    }

    public <T> Optional<T> queryForOptional(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        final List<T> results = query(sql, rowMapper, args);
        if (results.size() > 1) {
            throw new IncorrectResultSizeDataAccessException(1, results.size());
        }
        return results.stream().findFirst();
    }

    private <T> T execute(final String sql, final Object[] args, final PreparedStatementCallback<T> action) {
        try (final var conn = dataSource.getConnection();
             final var pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);

            setParameters(pstmt, args);
            return action.doInPreparedStatement(pstmt);
        } catch (SQLException e) {
            throw new DataAccessException("Failed to execute SQL: " + sql, e);
        }
    }

    private void setParameters(final PreparedStatement pstmt, final Object[] args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            pstmt.setObject(i + 1, args[i]);
        }
    }

    private <T> List<T> mapRows(final PreparedStatement pstmt, final RowMapper<T> rowMapper) throws SQLException {
        try (final var rs = pstmt.executeQuery()) {
            final List<T> results = new ArrayList<>();
            int rowNum = 0;
            while (rs.next()) {
                results.add(rowMapper.mapRow(rs, rowNum++));
            }
            return results;
        }
    }

    @FunctionalInterface
    private interface PreparedStatementCallback<T> {

        T doInPreparedStatement(PreparedStatement pstmt) throws SQLException;
    }
}
