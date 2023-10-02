package org.springframework.jdbc.core;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcTemplate {

    private final QueryAgent queryAgent;

    public JdbcTemplate(final DataSource dataSource) {
        this.queryAgent = new QueryAgent(dataSource);
    }

    public int update(final String sql, final Object... args) {
        QueryCallback<Integer> callback = PreparedStatement::executeUpdate;
        return queryAgent.service(sql, callback, args);
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
        QueryCallback<List<T>> callback = preparedStatement -> {
            ResultSet resultSet = preparedStatement.executeQuery();

            final List<T> results = new ArrayList<>();
            while (resultSet.next()) {
                results.add(rowMapper.map(resultSet));
            }
            return results;
        };

        return queryAgent.service(sql, callback, args);
    }
}
