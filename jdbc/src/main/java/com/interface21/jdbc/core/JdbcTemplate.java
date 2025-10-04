package com.interface21.jdbc.core;

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
        try (final var connection = dataSource.getConnection();
             final var preparedStatement = connection.prepareStatement(sql)
        ) {
            final var rs = executeQuery(args, preparedStatement);

            if (rs.next()) {
                return Optional.of(rowMapper.mapRow(rs));
            }

            return Optional.empty();
        } catch (final SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public <T> List<T> query(
            final String sql,
            final RowMapper<T> rowMapper,
            final Object... args
    ) {
        final List<T> results = new ArrayList<>();
        try (final var connection = dataSource.getConnection();
             final var preparedStatement = connection.prepareStatement(sql)
        ) {
            final var rs = executeQuery(args, preparedStatement);

            while (rs.next()) {
                results.add(rowMapper.mapRow(rs));
            }

            return results;
        } catch (final SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public int update(
            final String sql,
            final Object... args
    ) {
        try (final var connection = dataSource.getConnection();
             final var preparedStatement = connection.prepareStatement(sql)
        ) {
            buildParams(args, preparedStatement);

            return preparedStatement.executeUpdate();
        } catch (final SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private ResultSet executeQuery(
            final Object[] args,
            final PreparedStatement ps
    ) throws SQLException {
        buildParams(args, ps);

        return ps.executeQuery();
    }

    private void buildParams(
            final Object[] args,
            final PreparedStatement ps
    ) throws SQLException {
        for (int i = 1; i <= args.length; i++) {
            ps.setObject(i, args[i - 1]);
        }
    }
}
