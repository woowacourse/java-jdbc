package com.interface21.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... parameters) {
        try (
                final Connection connection = dataSource.getConnection();
                final PreparedStatement preparedStatement = connection.prepareStatement(sql)
        ) {
            setPreparedStatementParameters(preparedStatement, parameters);
            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                log.debug("query : {}", sql);
                if (resultSet.next()) {
                    return rowMapper.mapRow(resultSet, 1);
                }
                return null;
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private void setPreparedStatementParameters(
            final PreparedStatement preparedStatement,
            final Object... parameters
    ) throws SQLException {
        for (int i = 0; i < parameters.length; i++) {
            preparedStatement.setObject(i + 1, parameters[i]);
        }
    }
}
