package org.springframework.jdbc.core;

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

    public void update(String sql, Object... args) {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = getPreparedStatement(sql, connection, args);
        ) {
            log.debug("query : {}", sql);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        log.debug("query : {}", sql);

        return execute(sql, (resultSet) -> {
            if (resultSet.next()) {
                return rowMapper.mapRow(resultSet);
            }
            return null;
        }, args);
    }

    private <T> T execute(String sql, ResultSetExecutor<T> resultSetExecutor, Object... args) {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = getPreparedStatement(sql, connection, args);
                ResultSet rs = preparedStatement.executeQuery();
        ) {
            return resultSetExecutor.execute(rs);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) {
        log.debug("query : {}", sql);
        return execute(sql, (resultSet) -> {
            List<T> results = new ArrayList<>();
            while (resultSet.next()) {
                results.add(rowMapper.mapRow(resultSet));
            }
            return results;
        }, args);
    }

    private PreparedStatement getPreparedStatement(String sql, Connection connection, Object... args) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        setValues(preparedStatement, args);
        return preparedStatement;
    }

    private void setValues(PreparedStatement preparedStatement, Object... args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            preparedStatement.setObject(i + 1, args[i]);
        }
    }
}
