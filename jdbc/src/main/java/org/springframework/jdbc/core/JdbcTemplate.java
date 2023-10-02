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

    public void update(final String sql, final Object... parameters) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement preparedStatement = getPreparedStatement(sql, connection, parameters)) {
            log.debug("query : {}", sql);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public <T> List<T> queryForList(final String sql, final RowMapper<T> rowMapper, final Object... parameters) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement preparedStatement = getPreparedStatement(sql, connection, parameters);
             final ResultSet resultSet = preparedStatement.executeQuery()) {
            log.debug("query : {}", sql);
            final List<T> rsult = new ArrayList<>();
            while (resultSet.next()) {
                rsult.add(rowMapper.mapRow(resultSet));
            }
            return rsult;
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
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
        return null;
    }

    private PreparedStatement getPreparedStatement(
            final String sql,
            final Connection connection,
            final Object[] parameters
    ) throws SQLException {
        final PreparedStatement preparedStatement = connection.prepareStatement(sql);
        for (int index = 0; index < parameters.length; index++) {
            preparedStatement.setObject(index + 1, parameters[index]);
        }
        return preparedStatement;
    }
}
