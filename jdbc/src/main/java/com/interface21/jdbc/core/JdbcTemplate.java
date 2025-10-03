package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(
            final String sql,
            final PreparedStatementSetter preparedStatementSetter
    ) {
        try (final var connection = dataSource.getConnection();
             final var preparedStatement = connection.prepareStatement(sql)) {

            preparedStatementSetter.execute(preparedStatement);

            preparedStatement.executeUpdate();

            logSql(sql);

        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public <T> Optional<T> queryForObject(
            final String sql,
            final RowMapper<T> rowMapper,
            final PreparedStatementSetter preparedStatementSetter
    ) {
        final List<T> results = queryForList(sql, rowMapper, preparedStatementSetter);

        if (results.isEmpty()) {
            return Optional.empty();
        }
        if (results.size() > 1) {
            throw new DataAccessException("Incorrect result size: expected 1, but got " + results.size());
        }
        return Optional.of(results.getFirst());
    }

    public <T> List<T> queryForList(
            final String sql,
            final RowMapper<T> rowMapper,
            final PreparedStatementSetter preparedStatementSetter
    ) {
        try (final var connection = dataSource.getConnection();
             final var preparedStatement = connection.prepareStatement(sql)) {

            preparedStatementSetter.execute(preparedStatement);

            try (final var queryResultSet = preparedStatement.executeQuery()) {
                logSql(sql);

                final var objectMappingResultSet = new ArrayList<T>();

                while (queryResultSet.next()) {
                    objectMappingResultSet.add(rowMapper.mapRow(queryResultSet));
                }
                return objectMappingResultSet;
            }
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private void logSql(String sql) {
        log.info("query: {}", sql);
    }
}
