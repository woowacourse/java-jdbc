package org.springframework.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;

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

    public <T> Optional<T> queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return baseJdbcTemplate.execute(sql,
                preparedStatement -> {
                    setArguments(args, preparedStatement);
                    final List<T> results = getResults(rowMapper, preparedStatement.executeQuery());
                    return Optional.ofNullable(getResult(results));
                });
    }

    private <T> T getResult(List<T> results) {
        if (results.isEmpty()) {
            return null;
        }

        if (results.size() > 1) {
            throw new DataAccessException("조회된 데이터 수가 1을 초과합니다");
        }

        return results.get(0);
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper) {
        return baseJdbcTemplate.execute(sql,
                preparedStatement -> getResults(rowMapper, preparedStatement.executeQuery()));
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

    public void execute(final String sql) {
        baseJdbcTemplate.execute(sql, PreparedStatement::execute);
    }
}
