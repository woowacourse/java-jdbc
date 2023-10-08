package org.springframework.jdbc.core;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.dao.DataAccessException;

public class JdbcTemplate {

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(final String sql, final Object... parameters) {
        execute(new PreparedStatementCreator(sql), statement -> {
            final var statementSetter = newArgumentPreparedStatementSetter(parameters);
            statementSetter.setValues(statement);
            return statement.executeUpdate();
        });
    }

    public <T> T execute(PreparedStatementCreator statementCreator, StatementCallback<T> action) {
        try (var connection = dataSource.getConnection();
             var statement = statementCreator.createPreparedStatement(connection)) {
            return action.doInStatement(statement);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private ArgumentPreparedStatementSetter newArgumentPreparedStatementSetter(final Object... parameters) {
        return new ArgumentPreparedStatementSetter(parameters);
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... parameters) {
        final List<T> result = query(sql, rowMapper, parameters);
        if (result.size() != 1) {
            throw new IllegalStateException("Query 조회 결과가 하나가 아닙니다.");
        }
        return result.iterator().next();
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... parameters) {
        return execute(new PreparedStatementCreator(sql), statement -> {
            final var statementSetter = new ArgumentPreparedStatementSetter(parameters);
            statementSetter.setValues(statement);
            final var resultSet = statement.executeQuery();
            return result(rowMapper, resultSet);
        });
    }

    private <T> List<T> result(final RowMapper<T> rowMapper, final ResultSet resultSet) throws SQLException {
        List<T> result = new ArrayList<>();
        while (resultSet.next()) {
            result.add(rowMapper.mapRow(resultSet));
        }
        return result;
    }
}
