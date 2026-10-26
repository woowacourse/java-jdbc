package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(final String sql, final Object... parameters) {
        return update(sql, preparedStatement -> setParameters(preparedStatement, parameters));
    }

    public int update(final String sql, final PreparedStatementSetter setter) {
        log.debug("query : {}", sql);
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            setter.setValues(preparedStatement);
            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... parameters) {
        return query(sql, rowMapper, preparedStatement -> setParameters(preparedStatement, parameters));
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final PreparedStatementSetter setter) {
        log.debug("query : {}", sql);
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            setter.setValues(preparedStatement);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                List<T> results = new ArrayList<>();
                while (resultSet.next()) {
                    results.add(rowMapper.mapRow(resultSet));
                }
                return results;
            }
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... parameters) {
        List<T> results = query(sql, rowMapper, parameters);
        if (results.size() > 1) {
            throw new DataAccessException("Expected at most one row, but got " + results.size());
        }
        return results.isEmpty() ? null : results.get(0);
    }

    private static void setParameters(final PreparedStatement preparedStatement, final Object... parameters) throws SQLException {
        for (int index = 0; index < parameters.length; index++) {
            preparedStatement.setObject(index + 1, parameters[index]);
        }
    }
}
