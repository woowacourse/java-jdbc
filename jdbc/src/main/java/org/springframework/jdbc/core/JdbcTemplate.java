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
import java.util.Optional;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> List<T> queryForObjectsWithParameter(final String sql,
                                                    final RowMapper<T> rowMapper,
                                                    final Object... objects) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement preparedStatement = initializePreparedStatement(connection, sql, objects);
             final ResultSet resultSet = preparedStatement.executeQuery()) {
            final List<T> list = new ArrayList<>();
            while (resultSet.next()) {
                list.add(rowMapper.mapRow(resultSet));
            }
            return list;
        } catch (final SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public <T> Optional<T> queryForObjectWithParameter(final String sql,
                                                       final RowMapper<T> rowMapper,
                                                       final Object... objects) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement preparedStatement = initializePreparedStatement(connection, sql, objects);
             final ResultSet resultSet = preparedStatement.executeQuery()) {
            log.debug("query : {}", sql);
            if (resultSet.next()) {
                return Optional.of(rowMapper.mapRow(resultSet));
            }
            return Optional.empty();
        } catch (final SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public void update(final String sql,
                       final Object... objects) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement preparedStatement = initializePreparedStatement(connection, sql, objects)) {
            preparedStatement.executeUpdate();
        } catch (final SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private PreparedStatement initializePreparedStatement(final Connection connection,
                                                          final String sql,
                                                          final Object[] objects) throws SQLException {
        final PreparedStatement preparedStatement = connection.prepareStatement(sql);
        for (int i = 0; i < objects.length; i++) {
            preparedStatement.setObject(i + 1, objects[i]);
        }
        return preparedStatement;
    }
}
