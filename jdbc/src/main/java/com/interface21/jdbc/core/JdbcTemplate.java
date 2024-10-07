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

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(final String sql, final Object... params) {
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = getPreparedStatement(sql, connection)) {
            setPreparedStatement(params, preparedStatement);
            preparedStatement.executeUpdate();
            log.debug("query : {}", sql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... params) {
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = getPreparedStatement(sql, connection);
             ResultSet resultSet = getResultSet(params, preparedStatement)) {
            log.debug("query : {}", sql);
            resultSet.next();
            if(!resultSet.isLast()){
                throw new IllegalStateException("query result set has more than one row");
            }
            return rowMapper.mapRow(resultSet, resultSet.getRow());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... params) {
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = getPreparedStatement(sql, connection);
             ResultSet resultSet = getResultSet(params, preparedStatement)) {
            List<T> result = new ArrayList<>();
            log.debug("query : {}", sql);
            while (resultSet.next()) {
                result.add(rowMapper.mapRow(resultSet, resultSet.getRow()));
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    private void setPreparedStatement(Object[] params, PreparedStatement preparedStatement) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            preparedStatement.setObject(i + 1, params[i]);
        }
    }

    private PreparedStatement getPreparedStatement(String sql, Connection connection) throws SQLException {
        return connection.prepareStatement(sql);
    }

    private ResultSet getResultSet(Object[] params, PreparedStatement preparedStatement) throws SQLException {
        setPreparedStatement(params, preparedStatement);
        return preparedStatement.executeQuery();
    }
}
