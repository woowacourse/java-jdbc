package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class JdbcTemplate {

    private final DataSource dataSource;

    public <T> Optional<T> queryForObject(
            final String sql,
            final RowMapper<T> rowMapper,
            final Object... args
    ) {
        final List<T> query = query(sql, rowMapper, args);
        if (query.size() > 1) {
            throw new DataAccessException("결과가 2개 이상입니다: " + sql);
        }
        return query.stream().findFirst();
    }

    public <T> List<T> query(
            final String sql,
            final RowMapper<T> rowMapper,
            final Object... args
    ) {
        return execute(sql, args, ps -> {
            try (final ResultSet rs = ps.executeQuery()) {
                final List<T> results = new ArrayList<>();
                while (rs.next()) {
                    results.add(rowMapper.mapRow(rs));
                }
                return results;
            }
        });
    }

    public int update(
            final String sql,
            final Object... args
    ) {
        return execute(sql, args, PreparedStatement::executeUpdate);
    }

    private <T> T execute(
            final String sql,
            final Object[] args,
            final PreparedStatementCallback<T> callback
    ) {
        try (final var connection = dataSource.getConnection();
             final var preparedStatement = connection.prepareStatement(sql)) {
            buildParams(args, preparedStatement);
            return callback.execute(preparedStatement);
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private void buildParams(
            final Object[] args,
            final PreparedStatement ps
    ) throws SQLException {
        for (int i = 1; i <= args.length; i++) {
            ps.setObject(i, args[i - 1]);
        }
    }

    @FunctionalInterface
    private interface PreparedStatementCallback<T> {
        T execute(PreparedStatement ps) throws SQLException;
    }
}
