package org.springframework.jdbc.core;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcTemplate {

    private final StatementAgent statementAgent;

    public JdbcTemplate(final DataSource dataSource) {
        this.statementAgent = new StatementAgent(dataSource);
    }

    public int update(final Connection connection, final String sql, final Object... args) {
        StatementCallback<Integer> callback = PreparedStatement::executeUpdate;
        return statementAgent.service(connection, sql, callback, args);
    }

    public int update(final String sql, final Object... args) {
        StatementCallback<Integer> callback = PreparedStatement::executeUpdate;
        return statementAgent.service(sql, callback, args);
    }

    public <T> Optional<T> selectForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        List<T> results = selectAll(sql, rowMapper, args);
        validateHasUniqueResult(results);

        return results.stream()
                .findAny();
    }

    private <T> void validateHasUniqueResult(List<T> results) {
        if (results.size() != 1) {
            throw new ResultNotUniqueException();
        }
    }

    public <T> List<T> selectAll(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        StatementCallback<List<T>> callback = preparedStatement -> {
            ResultSet resultSet = preparedStatement.executeQuery();

            final List<T> results = new ArrayList<>();
            while (resultSet.next()) {
                results.add(rowMapper.map(resultSet, resultSet.getRow()));
            }
            return results;
        };

        return statementAgent.service(sql, callback, args);
    }
}
