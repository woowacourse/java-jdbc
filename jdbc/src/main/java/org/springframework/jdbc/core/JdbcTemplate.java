package org.springframework.jdbc.core;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;

public class JdbcTemplate {

    private static final int START_STATEMENT_INDEX = 1;

    private final PreparedStatementTemplate preparedStatementTemplate;
    private final ResultSetTemplate resultSetTemplate;

    public JdbcTemplate(final DataSource dataSource) {
        this.preparedStatementTemplate = new PreparedStatementTemplate(dataSource);
        this.resultSetTemplate = new ResultSetTemplate();
    }

    public int query(final String sql, final Object... statements) {
        return preparedStatementTemplate.execute(
                connection -> bindStatements().bind(connection.prepareStatement(sql), statements),
                PreparedStatement::executeUpdate
        );
    }

    public <T> Optional<T> queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... statements) {
        return preparedStatementTemplate.execute(
                connection -> bindStatements().bind(connection.prepareStatement(sql), statements),
                preparedStatement -> resultSetTemplate.resultSetExecute(
                        preparedStatement,
                        resultSet -> {
                            if (resultSet.next()) {
                                return Optional.of(rowMapper.mapRow(resultSet));
                            }

                            return Optional.empty();
                        }
                )
        );
    }

    public <T> List<T> queryForList(final String sql, final RowMapper<T> rowMapper, final Object... statements) {
        return preparedStatementTemplate.execute(
                connection -> bindStatements().bind(connection.prepareStatement(sql), statements),
                preparedStatement -> resultSetTemplate.resultSetExecute(
                        preparedStatement,
                        resultSet -> {
                            final List<T> result = new ArrayList<>();

                            while (resultSet.next()) {
                                result.add(rowMapper.mapRow(resultSet));
                            }

                            return result;
                        }
                )
        );
    }

    private PreparedStatementBinder bindStatements() {
        return (preparedStatement, statements) -> {
            for (int i = START_STATEMENT_INDEX; i < statements.length + 1; i++) {
                preparedStatement.setObject(i, statements[i - START_STATEMENT_INDEX]);
            }

            return preparedStatement;
        };
    }
}
