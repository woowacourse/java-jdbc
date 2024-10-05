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
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement(sql);
            setStatement(statement, args);
            log.info("update query : {}", sql);
            statement.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        } finally {
            close(statement, connection, null);
        }
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement(sql);
            setStatement(statement, args);
            log.info("select query : {}", sql);
            rs = statement.executeQuery();
            final List<T> results = extractData(rs, rowMapper, args);
            if (results.isEmpty()) {
                throw new SQLException("sql 결과 데이터가 존재하지 않습니다.");
            }
            if (results.size() > 1) {
                throw new SQLException("sql 결과 데이터가 2개 이상 존재합니다.");
            }
            return results.get(0);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        } finally {
            close(statement, connection, rs);
        }
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            connection = dataSource.getConnection();
            statement = connection.prepareStatement(sql);
            log.info("select query : {}", sql);
            rs = statement.executeQuery();
            return extractData(rs, rowMapper);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        } finally {
            close(statement, connection, rs);
        }
    }

    private void setStatement(PreparedStatement statement, final Object... args)
            throws SQLException {
        int count = 1;
        for (final Object arg : args) {
            statement.setObject(count++, arg);
        }
    }

    private <T> List<T> extractData(final ResultSet rs, final RowMapper<T> rowMapper, final Object... args)
            throws SQLException {
        List<T> results = new ArrayList<>();
        while (rs.next()) {
            results.add(rowMapper.mapRow(rs, args.length));
        }
        return results;
    }

    private void close(final PreparedStatement statement, final Connection connection, final ResultSet rs) {
        try {
            if (statement != null) {
                statement.close();
            }
        } catch (SQLException ignored) {
        }

        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException ignored) {
        }

        try {
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException ignored) {
        }
    }
}
