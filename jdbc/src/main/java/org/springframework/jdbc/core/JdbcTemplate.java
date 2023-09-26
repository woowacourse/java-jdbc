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

    private static final int INDEX_OF_OBJECT = 0;

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            setObjects(args, preparedStatement);
            ResultSet resultSet = preparedStatement.executeQuery();

            List<T> results = new ArrayList<>();
            while (resultSet.next()) {
                results.add(rowMapper.mapToRow(resultSet));
            }

            return results;
        } catch (final SQLException e) {
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            setObjects(args, preparedStatement);
            ResultSet resultSet = preparedStatement.executeQuery();

            List<T> results = new ArrayList<>();
            if (resultSet.next()) {
                T result = rowMapper.mapToRow(resultSet);
                results.add(result);
            }

            return results.get(INDEX_OF_OBJECT);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void update(final String sql, final Object... args) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            setObjects(args, preparedStatement);
            preparedStatement.executeUpdate();
        } catch (final SQLException e) {
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    private void setObjects(final Object[] args, final PreparedStatement preparedStatement) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            preparedStatement.setObject(i + 1, args[i]);
        }
    }
}
