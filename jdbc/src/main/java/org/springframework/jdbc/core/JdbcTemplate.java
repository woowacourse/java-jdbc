package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.dao.DataAccessException;

public class JdbcTemplate {

    private final QueryTemplate queryTemplate;

    public JdbcTemplate(DataSource dataSource) {
        this.queryTemplate = new QueryTemplate(dataSource);
    }

    public void update(Connection connection, String sql, Object... args) {
        queryTemplate.update(connection, sql, PreparedStatement::executeUpdate, args);
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) {
        return queryTemplate.query(sql, (resultSet -> mapResultToList(resultSet, rowMapper)), args);
    }

    private <T> List<T> mapResultToList(ResultSet resultSet, RowMapper<T> rowMapper) throws SQLException {
        List<T> results = new ArrayList<>();

        while (resultSet.next()) {
            T result = rowMapper.mapRow(resultSet);

            results.add(result);
        }

        return results;
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        return queryTemplate.query(sql, resultSet -> mapResultToObject(resultSet, rowMapper), args);
    }

    private <T> T mapResultToObject(ResultSet resultSet, RowMapper<T> rowMapper) throws SQLException {
        if (resultSet.next()) {
            T result = rowMapper.mapRow(resultSet);

            validateSingleResult(resultSet);

            return result;
        }

        throw new DataAccessException("Incorrect Result Size ! Result is null");
    }

    private void validateSingleResult(ResultSet resultSet) throws SQLException {
        if (resultSet.next()) {
            throw new DataAccessException("Incorrect Result Size ! Result  must be one");
        }
    }

}
