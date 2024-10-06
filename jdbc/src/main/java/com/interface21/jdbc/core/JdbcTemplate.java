package com.interface21.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.interface21.dao.DataAccessException;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(final String sql, final Object... args) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement statement = connection.prepareStatement(sql)) {
            setStatement(statement, args);
            log.info("update query : {}", sql);
            statement.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement statement = connection.prepareStatement(sql)) {
            setStatement(statement, args);
            log.info("select query : {}", sql);
            final ResultSet resultSet = statement.executeQuery();
            final List<T> data = extractData(resultSet, rowMapper, args);
            return result(data);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper) {
        try(final Connection connection = dataSource.getConnection();
            final PreparedStatement statement = connection.prepareStatement(sql)) {
            log.info("select query : {}", sql);
            final ResultSet resultSet = statement.executeQuery();
            return extractData(resultSet, rowMapper);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private void setStatement(PreparedStatement statement, final Object... args)
            throws SQLException {
        int index = 1;
        for (final Object arg : args) {
            statement.setObject(index++, arg);
        }
    }

    private <T> List<T> extractData(final ResultSet resultSet, final RowMapper<T> rowMapper, final Object... args)
            throws SQLException {
        final List<T> results = new ArrayList<>();
        while (resultSet.next()) {
            results.add(rowMapper.mapRow(resultSet, args.length));
        }
        return results;
    }

    private <T> T result(final List<T> data) throws SQLException {
        if (data.isEmpty()) {
            throw new SQLException("sql 결과 데이터가 존재하지 않습니다.");
        }
        if (data.size() > 1) {
            throw new SQLException("sql 결과 데이터가 2개 이상 존재합니다.");
        }
        return data.getFirst();
    }
}
