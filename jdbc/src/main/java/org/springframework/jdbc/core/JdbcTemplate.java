package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(final String sql, final Object... objects) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement preparedStatement = prepareStatementWithBindingQuery(connection.prepareStatement(sql), objects)) {

            log.debug("query : {}", sql);

            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    private PreparedStatement prepareStatementWithBindingQuery(final PreparedStatement preparedStatement, final Object... objects) throws SQLException {
        for (int i = 0; i < objects.length; i++) {
            preparedStatement.setObject(i + 1, objects[i]);
        }
        return preparedStatement;
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... objects) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement preparedStatement = prepareStatementWithBindingQuery(connection.prepareStatement(sql), objects);
             final ResultSet resultSet = preparedStatement.executeQuery()) {

            final List<T> results = new ArrayList<>();

            log.debug("query : {}", sql);

            while (resultSet.next()) {
                results.add(rowMapper.mapping(resultSet));
            }
            return results;

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    public <T> Optional<T> queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... objects) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement preparedStatement = prepareStatementWithBindingQuery(connection.prepareStatement(sql), objects);
             final ResultSet resultSet = preparedStatement.executeQuery()) {

            log.debug("query : {}", sql);

            if (resultSet.next()) {
                return Optional.of(rowMapper.mapping(resultSet));
            }
            return Optional.empty();

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e.getMessage(), e);
        }
    }
}
