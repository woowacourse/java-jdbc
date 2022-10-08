package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return query(sql, new ArgumentPreparedStatementSetter(args), new RowMapperResultSetExtractor<>(rowMapper));
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        final List<T> result = query(sql, new ArgumentPreparedStatementSetter(args),
                new RowMapperResultSetExtractor<>(rowMapper));

        if (result.size() > 1) {
            throw new DataAccessException("Incorrect result size: expected 1, actual " + result.size());
        }
        return result.iterator().next();
    }

    private <T> T query(final String sql, final PreparedStatementSetter preparedStatementSetter,
                        final ResultSetExtractor<T> resultSetExtractor) {
        log.debug("execute SQL query [{}]", sql);

        return execute(sql, preparedStatement -> {
            preparedStatementSetter.setValues(preparedStatement);
            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSetExtractor.extractData(resultSet);
            }
        });
    }

    public int update(final String sql, final Object... args) {
        return update(sql, new ArgumentPreparedStatementSetter(args));
    }

    private int update(final String sql, final PreparedStatementSetter preparedStatementSetter) {
        log.debug("execute SQL update [{}]", sql);

        return execute(sql, preparedStatement -> {
            preparedStatementSetter.setValues(preparedStatement);
            return preparedStatement.executeUpdate();
        });
    }

    private <T> T execute(final String sql, final PreparedStatementCallback<T> action) {
        if (sql == null) {
            throw new IllegalArgumentException("SQL must not be null");
        }

        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement statement = connection.prepareStatement(sql)) {
            return action.doInPreparedStatement(statement);
        } catch (final SQLException e) {
            throw new DataAccessException(e);
        }
    }
}
