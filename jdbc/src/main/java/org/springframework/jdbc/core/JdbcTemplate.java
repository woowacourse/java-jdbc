package org.springframework.jdbc.core;

import static java.util.Objects.requireNonNull;

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

    public void update(final String sql, final Object... args) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            setArgument(args, preparedStatement);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        try (final Connection connection = requireNonNull(dataSource).getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            setArgument(args, preparedStatement);

            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return rowMapper.mapRow(resultSet, resultSet.getRow());
                }
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
        return null;
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        try (final Connection connection = requireNonNull(dataSource).getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            setArgument(args, preparedStatement);

            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                return getResults(rowMapper, resultSet);
            }

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private <T> List<T> getResults(final RowMapper<T> rowMapper, final ResultSet resultSet) throws SQLException {
        final ArrayList<T> results = new ArrayList<>();
        while (resultSet.next()) {
            final T result = rowMapper.mapRow(resultSet, resultSet.getRow());
            results.add(result);
        }
        return results;
    }

    private void setArgument(final Object[] args, final PreparedStatement preparedStatement) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            preparedStatement.setObject(i + 1, args[i]);
        }
    }
}
