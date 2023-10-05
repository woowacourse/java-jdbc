package org.springframework.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(final String sql, final Object... objects) {
        execute(sql, PreparedStatement::executeUpdate, objects);
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... objects) {
        final var results = execute(sql, preparedStatement -> mapResults(rowMapper, preparedStatement), objects);
        if (results.size() != 1) {
            throw new DataAccessException("1개의 데이터만 조회되어야 합니다.");
        }
        return results.get(0);
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper) {
        return execute(sql, preparedStatement -> mapResults(rowMapper, preparedStatement));
    }

    private <T> List<T> mapResults(final RowMapper<T> rowMapper, final PreparedStatement preparedStatement)
            throws SQLException {
        final var resultSet = preparedStatement.executeQuery();
        final var results = new ArrayList<T>();
        while (resultSet.next()) {
            results.add(rowMapper.mapRow(resultSet));
        }
        return results;
    }

    private PreparedStatement prepareStatement(final PreparedStatement preparedStatement, final Object[] objects)
            throws SQLException {
        for (int i = 0; i < objects.length; i++) {
            preparedStatement.setObject(i + 1, objects[i]);
        }
        return preparedStatement;
    }

    private <T> T execute(final String sql, final PreparedStatementCallback<T> preparedStatementCallback,
                          final Object... objects) {
        try (final var connection = dataSource.getConnection();
             final var preparedStatement = prepareStatement(connection.prepareStatement(sql), objects)) {
            log.debug("query : {}", sql);
            return preparedStatementCallback.callback(preparedStatement);
        } catch (SQLException e) {
            log.error("쿼리 실행 과정에서 오류가 발생했습니다. - query : {}, parameters : {}", sql, objects);
            throw new DataAccessException(e);
        }
    }
}
