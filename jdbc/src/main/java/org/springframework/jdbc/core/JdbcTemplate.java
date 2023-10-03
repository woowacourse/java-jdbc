package org.springframework.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final BaseJdbcTemplate baseJdbcTemplate;

    public JdbcTemplate(final DataSource dataSource) {
        this.baseJdbcTemplate = new BaseJdbcTemplate(dataSource);
    }

    public void update(final String sql, final Object... args) {
        baseJdbcTemplate.execute(sql,
                preparedStatement -> {
                    setArguments(args, preparedStatement);
                    preparedStatement.executeUpdate();
                    return null;
                }
        );
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return baseJdbcTemplate.execute(sql,
                preparedStatement -> {
                    setArguments(args, preparedStatement);
                    try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                        if (resultSet.next()) {
                            return rowMapper.mapRow(resultSet, resultSet.getRow());
                        }
                        return null;
                    }
                });
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper) {
        return baseJdbcTemplate.execute(sql, preparedStatement -> {
            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                return getResults(rowMapper, resultSet);
            }
        });
    }

    private <T> List<T> getResults(final RowMapper<T> rowMapper, final ResultSet resultSet) throws SQLException {
        final ArrayList<T> results = new ArrayList<>();
        while (resultSet.next()) {
            final T result = rowMapper.mapRow(resultSet, resultSet.getRow());
            results.add(result);
        }
        return results;
    }

    private void setArguments(final Object[] args, final PreparedStatement preparedStatement) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            preparedStatement.setObject(i + 1, args[i]);
        }
    }
}
