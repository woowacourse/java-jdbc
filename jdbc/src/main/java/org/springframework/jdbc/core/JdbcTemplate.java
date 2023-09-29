package org.springframework.jdbc.core;

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

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... values) {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = prepareStatement(connection, sql, values);
                ResultSet resultSet = preparedStatement.executeQuery()
        ) {
            List<T> results = new ArrayList<>();
            while (resultSet.next()) {
                T result = rowMapper.mapRow(resultSet);
                results.add(result);
            }
            return results;
        } catch (SQLException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private PreparedStatement prepareStatement(
            Connection connection,
            String sql,
            Object... values
    ) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        for (int i = 0; i < values.length; i++) {
            preparedStatement.setObject(i + 1, values[i]);
        }
        return preparedStatement;
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... values) {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = prepareStatement(connection, sql, values);
                ResultSet resultSet = preparedStatement.executeQuery()
        ) {
            if (resultSet.next()) {
                return rowMapper.mapRow(resultSet);
            }
            return null;
        } catch (SQLException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void update(String sql, Object... values) {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = prepareStatement(connection, sql, values)
        ) {
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
