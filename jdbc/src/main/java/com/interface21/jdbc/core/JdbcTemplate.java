package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.EmptyResultDataAccessException;
import com.interface21.jdbc.NonUniqueResultException;
import java.sql.Connection;
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

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            setArguments(ps, args);
            return executeQuery(ps, rowMapper);
        } catch (final Exception e) {
            log.error("query error", e);
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    private void setArguments(PreparedStatement ps, Object[] args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            ps.setObject(i + 1, args[i]);
        }
    }

    private <T> List<T> executeQuery(PreparedStatement ps, RowMapper<T> rowMapper) throws SQLException {
        try (ResultSet resultSet = ps.executeQuery()) {
            return getResults(resultSet, rowMapper);
        }
    }

    private <T> List<T> getResults(ResultSet resultSet, RowMapper<T> rowMapper) throws SQLException {
        List<T> results = new ArrayList<>();
        while (resultSet.next()) {
            results.add(rowMapper.mapRow(resultSet, resultSet.getRow()));
        }
        return results;
    }

    public <T> T queryForObject(String sql, RowMapper<T> RowMapper, Object... args) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            setArguments(ps, args);
            return excuteQueryForObject(RowMapper, ps);
        } catch (final Exception e) {
            log.error("queryForObject error", e);
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    private <T> T excuteQueryForObject(RowMapper<T> rowMapper, PreparedStatement ps) throws SQLException {
        try (ResultSet resultSet = ps.executeQuery()) {
            return getSingleResult(resultSet, rowMapper);
        }
    }

    private <T> T getSingleResult(ResultSet resultSet, RowMapper<T> rowMapper) throws SQLException {
        if (resultSet.next()) {
            T result = rowMapper.mapRow(resultSet, resultSet.getRow());
            checkForRemainingResult(resultSet);
            return result;
        }
        throw new EmptyResultDataAccessException("No result");
    }

    private void checkForRemainingResult(ResultSet resultSet) throws SQLException {
        if (resultSet.next()) {
            throw new NonUniqueResultException("Query returned more than one result.");
        }
    }

    public int update(String sql, Object... args) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            setArguments(ps, args);
            return ps.executeUpdate();
        } catch (final Exception e) {
            log.error("update error", e);
            throw new DataAccessException(e.getMessage(), e);
        }
    }
}
