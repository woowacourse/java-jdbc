package org.springframework.jdbc.core;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.jdbc.core.exception.MultipleDataAccessException;

public class JdbcTemplate {

    private final PreparedStatementTemplate preparedStatementTemplate;
    private final ResultSetTemplate resultSetTemplate;

    public JdbcTemplate(final DataSource dataSource) {
        this.preparedStatementTemplate = new PreparedStatementTemplate(dataSource);
        this.resultSetTemplate = new ResultSetTemplate();
    }

    public int executeQuery(final String sql, final Object... statements) {
        return preparedStatementTemplate.execute(
                connection -> connection.prepareStatement(sql),
                PreparedStatement::executeUpdate,
                statements
        );
    }

    public <T> Optional<T> executeQueryForObject(
            final String sql,
            final RowMapper<T> rowMapper,
            final Object... statements
    ) {
        final ResultSetMapper<Optional<T>> resultSetMapper = resultSet -> {
            if (!resultSet.next()) {
                return Optional.empty();
            }

            final T result = rowMapper.mapRow(resultSet);

            if (resultSet.next()) {
                throw new MultipleDataAccessException("단일 결과가 아닙니다.");
            }

            return Optional.of(result);
        };

        return preparedStatementTemplate.execute(
                connection -> connection.prepareStatement(sql),
                preparedStatement -> resultSetTemplate.execute(preparedStatement, resultSetMapper),
                statements
        );
    }

    public <T> List<T> executeQueryForList(
            final String sql,
            final RowMapper<T> rowMapper,
            final Object... statements
    ) {
        final ResultSetMapper<List<T>> resultSetMapper = resultSet -> {
            final List<T> result = new ArrayList<>();

            while (resultSet.next()) {
                result.add(rowMapper.mapRow(resultSet));
            }

            return result;
        };

        return preparedStatementTemplate.execute(
                connection -> connection.prepareStatement(sql),
                preparedStatement -> resultSetTemplate.execute(preparedStatement, resultSetMapper),
                statements
        );
    }
}
