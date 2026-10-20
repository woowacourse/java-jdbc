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

    public int update(final String sql, final Object... args) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = dataSource.getConnection();
            preparedStatement = connection.prepareStatement(sql);

            log.debug("query : {}", sql);

            setParameters(preparedStatement, args);
            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        } finally {
            close(preparedStatement);
            close(connection);
        }
    }

    public Object queryForObject(final String sql, final RowMapper rowMapper, final Object... args) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = dataSource.getConnection();
            preparedStatement = connection.prepareStatement(sql);
            setParameters(preparedStatement, args);
            resultSet = preparedStatement.executeQuery();

            log.debug("query : {}", sql);

            if (resultSet.next()) {
                return rowMapper.mapRow(resultSet);
            }
            return null;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        } finally {
            close(resultSet);
            close(preparedStatement);
            close(connection);
        }
    }

    public List<Object> query(final String sql, final RowMapper rowMapper, final Object... args) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = dataSource.getConnection();
            preparedStatement = connection.prepareStatement(sql);
            setParameters(preparedStatement, args);
            resultSet = preparedStatement.executeQuery();

            log.debug("query : {}", sql);

            final List<Object> results = new ArrayList<>();
            while (resultSet.next()) {
                results.add(rowMapper.mapRow(resultSet));
            }
            return results;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        } finally {
            close(resultSet);
            close(preparedStatement);
            close(connection);
        }
    }

    private void setParameters(final PreparedStatement preparedStatement, final Object... args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            preparedStatement.setObject(i + 1, args[i]);
        }
    }

    private void close(final AutoCloseable resource) {
        if (resource == null) {
            return;
        }
        try {
            resource.close();
        } catch (Exception ignored) {}
    }
}
