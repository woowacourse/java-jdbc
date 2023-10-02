package org.springframework.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final PreparedStatementExecutor preparedStatementExecutor;

    public JdbcTemplate(final DataSource dataSource) {
        this.preparedStatementExecutor = new PreparedStatementExecutor(dataSource);
    }

    public void update(final String sql, final Object... objects) {
        preparedStatementExecutor.execute(PreparedStatement::executeUpdate, sql, objects);
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... objects) {
        return preparedStatementExecutor.execute(preparedStatement -> {
            final ResultSet resultSet = preparedStatement.executeQuery();
            final List<T> results = new ArrayList<>();
            while (resultSet.next()) {
                results.add(rowMapper.mapping(resultSet));
            }
            return results;
        }, sql, objects);
    }

    public <T> Optional<T> queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... objects) {
        return preparedStatementExecutor.execute(preparedStatement -> {
            final ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return Optional.of(rowMapper.mapping(resultSet));
            }
            return Optional.empty();
        }, sql, objects);
    }
}
