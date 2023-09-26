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

    public void update(final String sql, final Object... parameters) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement preparedStatement = getPreparedStatement(sql, connection, parameters)) {
            log.debug("query : {}", sql);
            for (int i = 0; i < parameters.length; i++) {
                preparedStatement.setObject(i + 1, parameters[i]);
            }
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... parameters) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement preparedStatement = getPreparedStatement(sql, connection, parameters);
             final ResultSet resultSet = preparedStatement.executeQuery()) {
            log.debug("query : {}", sql);
            if (resultSet.next()) {
                return rowMapper.mapRow(resultSet);
            }
            return null;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private PreparedStatement getPreparedStatement(
            final String sql,
            final Connection connection,
            final Object[] parameters
    ) throws SQLException {
        final PreparedStatement preparedStatement = connection.prepareStatement(sql);
        for (int i = 0; i < parameters.length; i++) {
            preparedStatement.setObject(i + 1, parameters[i]);
        }
        return preparedStatement;
    }

    public <T> List<T> queryForList(final String sql, final RowMapper<T> rowMapper, final Object... parameters) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement preparedStatement = getPreparedStatement(sql, connection, parameters);
             final ResultSet resultSet = preparedStatement.executeQuery()) {
            log.debug("query : {}", sql);
            final List<T> result = new ArrayList<>();
            while (resultSet.next()) {
                result.add(rowMapper.mapRow(resultSet));
            }
            return result;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
