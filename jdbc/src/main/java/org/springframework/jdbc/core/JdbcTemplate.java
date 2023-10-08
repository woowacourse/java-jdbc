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

public class JdbcTemplate {

    private static Logger log = LoggerFactory.getLogger(JdbcTemplate.class);
    private final DataSource dataSource;

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

    public void update(Connection connection, String sql, Object... args) {
        update(connection, sql, createPreparedStatementSetter(args));
    }

    public void update(String sql, Object... args) {
        update(sql, createPreparedStatementSetter(args));
    }

    private <T> List<T> query(Connection connection, String sql, RowMapper<T> rowMapper, PreparedStatementSetter pss) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            pss.setParameters(preparedStatement);
            return mapResultSetToObject(rowMapper, preparedStatement);
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

    private void update(Connection connection, String sql, PreparedStatementSetter pss) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            pss.setParameters(preparedStatement);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private void update(String sql, PreparedStatementSetter pss) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            pss.setParameters(preparedStatement);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    private PreparedStatementSetter createPreparedStatementSetter(Object... args) {
        return psmt -> {
            for (int i = 0; i < args.length; i++) {
                psmt.setObject(i + 1, args[i]);
            }
        };
    }

        return preparedStatement;
    }
}
