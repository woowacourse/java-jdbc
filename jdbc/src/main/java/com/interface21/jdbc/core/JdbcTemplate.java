package com.interface21.jdbc.core;

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

    public <T> T queryForObject(final String sql, final RowMapper rowMapper, final Object... conditions) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)
        ) {
            log.debug("query : {}", sql);

            for (int argumentIndex = 0; argumentIndex < conditions.length; argumentIndex++) {
                preparedStatement.setObject(argumentIndex + 1, conditions[argumentIndex]);
            }

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return rowMapper.mapRow(resultSet);
                }
                return null;
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new JdbcException("An error occurred during the execution of the select query.", e);
        }
    }

    public <T> List<T> query(final String sql, final RowMapper rowMapper) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()
        ) {
            log.debug("query : {}", sql);

            List<T> results = new ArrayList<>();
            while (resultSet.next()) {
                results.add(rowMapper.mapRow(resultSet));
            }
            return results;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new JdbcException("An error occurred during the execution of the select query.", e);
        }
    }

    public void update(final String sql, final Object... arguments) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)
        ) {
            log.debug("query : {}", sql);

            for (int argumentIndex = 0; argumentIndex < arguments.length; argumentIndex++) {
                preparedStatement.setObject(argumentIndex + 1, arguments[argumentIndex]);
            }

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new JdbcException("An error occurred during the execution of the update query.", e);
        }
    }
}
