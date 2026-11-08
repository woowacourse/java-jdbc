package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(final String sql, final Object... args) {
        log.debug("query : {}", sql);
        try (final var connection = dataSource.getConnection();
             final var statement = connection.prepareStatement(sql)) {
            bindParameters(statement, args);
            return statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to execute update: " + sql, e);
        }
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        log.debug("query : {}", sql);
        try (final var connection = dataSource.getConnection();
             final var statement = connection.prepareStatement(sql)) {
            bindParameters(statement, args);
            try (final var resultSet = statement.executeQuery()) {
                final List<T> results = new ArrayList<>();
                int rowNum = 0;
                while (resultSet.next()) {
                    results.add(rowMapper.mapRow(resultSet, rowNum++));
                }
                return results;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to execute query: " + sql, e);
        }
    }

    /**
     * 최대 한 건을 조회한다.
     * <p>
     * 조회 결과가 없으면 {@code null}을 반환하고, 두 건 이상이면 {@link DataAccessException}을 던진다.
     * 한 행이 존재하지만 {@link RowMapper}가 {@code null}을 반환한 경우도 {@code null}이므로,
     * "행 없음"과 구분할 필요가 있다면 {@link RowMapper}가 {@code null}을 반환하지 않도록 작성해야 한다.
     */
    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        final var results = query(sql, rowMapper, args);
        if (results.size() > 1) {
            throw new DataAccessException("Expected at most one row, but found " + results.size());
        }
        return results.isEmpty() ? null : results.getFirst();
    }

    private void bindParameters(final PreparedStatement statement, final Object... args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            statement.setObject(i + 1, args[i]);
        }
    }
}
