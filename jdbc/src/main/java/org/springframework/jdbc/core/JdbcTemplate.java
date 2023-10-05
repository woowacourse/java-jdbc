package org.springframework.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcTemplate {

    private static Logger log = LoggerFactory.getLogger(JdbcTemplate.class);
    private DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = getPreparedStatement(connection, sql, args);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            List<T> results = new ArrayList<>();
            while (resultSet.next()) {
                results.add(rowMapper.run(resultSet));
            }

            return results;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    public <T> Optional<T> queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = getPreparedStatement(connection, sql, args);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            if (!resultSet.next()) {
                return Optional.empty();
            }

            return Optional.of(rowMapper.run(resultSet));
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    public void update(String sql, Object... args) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = getPreparedStatement(connection, sql, args)) {
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    private PreparedStatement getPreparedStatement(Connection connection, String sql, Object[] args) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(sql);

        for (int i = 0; i < args.length; i++) {
            preparedStatement.setObject(i + 1, args[i]);
        }

        return preparedStatement;
    }
}
