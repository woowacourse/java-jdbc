package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);
    private static final int QUERY_PARAMETER_STEP = 1;

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... parameters) {
        try(final Connection connection = dataSource.getConnection();
            final PreparedStatement preparedStatement = createPreparedStatement(connection, sql, parameters);
        ){
            final List<T> result = new ArrayList<>();
            final ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                result.add(rowMapper.mapRow(resultSet));
            }
            return result;
        } catch (final SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public <T> Optional<T> queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... parameters) {
        try(final Connection connection = dataSource.getConnection();
            final PreparedStatement preparedStatement = createPreparedStatement(connection, sql, parameters);
        ){
            final ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return Optional.of(rowMapper.mapRow(resultSet));
            }
            return Optional.empty();
        }catch (final SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private PreparedStatement createPreparedStatement(
            final Connection connection,
            final String sql,
            final Object[] parameters
    ) throws SQLException {
        final PreparedStatement preparedStatement = connection.prepareStatement(sql);

        for(int index = 0; index < parameters.length; index++) {
            preparedStatement.setObject(index + QUERY_PARAMETER_STEP, parameters[index]);
        }

        return preparedStatement;
    }
}
