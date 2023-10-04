package org.springframework.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;

public class JdbcTemplate {

    private final QueryTemplate queryTemplate;

    public JdbcTemplate(final DataSource dataSource) {
        this.queryTemplate = new QueryTemplate(dataSource);
    }

    public void update(String sql, Object... args) {
        queryTemplate.service(sql, PreparedStatement::executeUpdate, args);
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        List<T> results = query(sql, rowMapper, args);
        validateSize(results);
        return results.get(0);
    }

    private <T> void validateSize(List<T> results) {
        if (results.isEmpty()) {
            throw new EmptyResultDataAccessException(1);
        }
        if (results.size() > 1) {
            throw new IncorrectResultSizeDataAccessException(1, results.size());
        }
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) {
        return queryTemplate.service(sql, prepareStatement -> {
            ResultSet resultSet = prepareStatement.executeQuery();
            List<T> result = new ArrayList<>();
            while (resultSet.next()) {
                result.add(rowMapper.mapRow(resultSet));
            }
            return result;
        }, args);
    }
}
