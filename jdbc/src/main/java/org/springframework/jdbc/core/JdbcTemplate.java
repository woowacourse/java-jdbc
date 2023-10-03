package org.springframework.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void executeUpdate(final String sql, final Object... parameters) {
        execute(sql, PreparedStatement::executeUpdate, parameters);
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... parameters) {
        return execute(sql, PreparedStatement::executeQuery, rowMapper, parameters);
    }

    private <T> T execute(final String sql, final QueryExecution execution,
                          final RowMapper<T> rowMapper, final Object... parameters) {
        try (final PreparedStatement preparedStatement = dataSource.getConnection().prepareStatement(sql)) {
            for (int i = 1; i <= parameters.length; i++) {
                preparedStatement.setObject(i, parameters[i - 1]);
            }
            log.debug("query : {}", preparedStatement);

            final ResultSet resultSet = (ResultSet) execution.execute(preparedStatement);
            return rowMapper.map(resultSet);
        } catch (Exception e) {
            throw new DataAccessException(e);
        }
    }

    private void execute(final String sql, final QueryExecution execution,
                         final Object... parameters) {
        try (final PreparedStatement preparedStatement = dataSource.getConnection().prepareStatement(sql)) {
            for (int i = 1; i <= parameters.length; i++) {
                preparedStatement.setObject(i, parameters[i - 1]);
            }
            log.debug("query : {}", preparedStatement);

            execution.execute(preparedStatement);
        } catch (Exception e) {
            throw new DataAccessException(e);
        }
    }

}
