package org.springframework.jdbc.core;

import static java.util.Objects.requireNonNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;

public class BaseJdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(BaseJdbcTemplate.class);

    private final DataSource dataSource;

    public BaseJdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T execute(final String sql,
                         final PreparedStatementAction<T> action) {
        try (
                final Connection connection = requireNonNull(dataSource).getConnection();
                final PreparedStatement preparedStatement = connection.prepareStatement(sql)
        ) {
            return action.execute(preparedStatement);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    public <T> T execute(final Connection connection,
                         final String sql,
                         final PreparedStatementAction<T> action) {
        try (
                final PreparedStatement preparedStatement = connection.prepareStatement(sql)
        ) {
            return action.execute(preparedStatement);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }
}
