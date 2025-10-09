package com.interface21.jdbc;

import com.interface21.dao.DataAccessException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

public class JdbcTemplate {

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(final String sql, final PreparedStatementSetter pss) {
        try (final var connection = dataSource.getConnection();
             final var preparedStatement = connection.prepareStatement(sql)) {
            pss.setValues(preparedStatement);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public void update(final String sql, final Object... args) {
        update(sql, getDefaultPreparedStatementSetter(args));
    }

    public <T> T queryForObject(
            final String sql,
            final RowMapper<T> rowMapper,
            final PreparedStatementSetter pss
    ) {
        final List<T> results = query(sql, rowMapper, pss);
        if (results.isEmpty()) {
            return null;
        }
        return results.getFirst();
    }

    public <T> T queryForObject(
            final String sql,
            final RowMapper<T> rowMapper,
            final Object... args
    ) {
        return queryForObject(sql, rowMapper, getDefaultPreparedStatementSetter(args));
    }

    public <T> List<T> query(
            final String sql,
            final RowMapper<T> rowMapper,
            final PreparedStatementSetter pss
    ) {
        try (final var connection = dataSource.getConnection();
             final var preparedStatement = connection.prepareStatement(sql)) {
            pss.setValues(preparedStatement);
            final var resultSet = preparedStatement.executeQuery();

            final List<T> results = new ArrayList<>();
            while (resultSet.next()) {
                results.add(rowMapper.mapRow(resultSet));
            }
            return results;

        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public <T> List<T> query(
            final String sql,
            final RowMapper<T> rowMapper,
            final Object... args
    ) {
        return query(sql, rowMapper, getDefaultPreparedStatementSetter(args));
    }

    private PreparedStatementSetter getDefaultPreparedStatementSetter(final Object[] args) {
        return preparedStatement -> {
            for (int i = 0; i < args.length; i++) {
                preparedStatement.setObject(i + 1, args[i]);
            }
        };
    }
}
