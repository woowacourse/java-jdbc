package org.springframework.jdbc.core;

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

    public void update(String sql, Object... args) {
        queryTemplate.update(sql, PreparedStatement::executeUpdate, args);
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) {
        return queryTemplate.query(sql, (resultSet -> mapResultToList(resultSet, rowMapper)), args);
    }

    private <T> List<T> mapResultToList(ResultSet rs, RowMapper<T> rowMapper) throws SQLException {
        List<T> results = new ArrayList<>();

        while (rs.next()) {
            T result = rowMapper.mapRow(rs);

            results.add(result);
        }

        return results;
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        return queryTemplate.query(sql, resultSet -> mapResultToObject(resultSet, rowMapper), args);
    }

    private <T> T mapResultToObject(ResultSet rs, RowMapper<T> rowMapper) throws SQLException {
        if (rs.next()) {
            T result = rowMapper.mapRow(rs);

            validateSingleResult(rs);

            return result;
        }

        throw new DataAccessException("Incorrect Result Size ! Result is null");
    }

    private void validateSingleResult(ResultSet rs) throws SQLException {
        if (rs.next()) {
            throw new DataAccessException("Incorrect Result Size ! Result  must be one");
        }
    }

}
