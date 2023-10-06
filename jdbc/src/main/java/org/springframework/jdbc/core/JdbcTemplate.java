package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import org.springframework.dao.DataAccessException;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... params) {
        List<T> results;
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement preparedStatement = conn.prepareStatement(sql);
             final ResultSet resultSet = executeQuery(preparedStatement, params)) {
            log.debug("query : {}", sql);
            results = mapResults(rowMapper, resultSet);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }

        if (results.size() > 1) {
            throw new DataAccessException("too many result. expected 1 but was " + results.size());
        }
        if (results.isEmpty()) {
            throw new DataAccessException("no result");
        }

        return results.get(0);
    }

    public <T> List<T> queryForObjects(String sql, RowMapper<T> rowMapper, Object... parameters) {
        List<T> results;

        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement preparedStatement = conn.prepareStatement(sql);
             final ResultSet resultSet = executeQuery(preparedStatement, parameters)) {
            log.debug("query : {}", sql);
            results = mapResults(rowMapper, resultSet);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }

        if (results.isEmpty()) {
            throw new DataAccessException("no result");
        }

        return results;
    }

    private ResultSet executeQuery(final PreparedStatement preparedStatement, final Object[] parameters)
            throws SQLException {
        setParameters(preparedStatement, parameters);
        return preparedStatement.executeQuery();
    }

    private void setParameters(final PreparedStatement preparedStatement, final Object[] parameters)
            throws SQLException {
        for (int i = 0; i < parameters.length; i++) {
            preparedStatement.setObject(i + 1, parameters[i]);
        }
    }

    private <T> List<T> mapResults(final RowMapper<T> rowMapper, final ResultSet resultSet) throws SQLException {
        final List<T> results = new ArrayList<>();
        while (resultSet.next()) {
            results.add(rowMapper.mapRow(resultSet, resultSet.getRow()));
        }
        return results;
    }

    public void update(String sql, Object... parameters) {
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);

            setParameters(preparedStatement, parameters);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

}
