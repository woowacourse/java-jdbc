package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;
    private final PreparedStatementSetter preparedStatementSetter = new PreparedStatementSetter();

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private <T> T execute(String sql, PreparedStatementCallback<T> statementCallback) {
        try (final var connection = dataSource.getConnection();
             final var preparedStatement = connection.prepareStatement(sql)) {
            return statementCallback.execute(preparedStatement);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) {
        return execute(sql, statement -> {
            preparedStatementSetter.setParams(statement, args);
            return executeQuery(statement, new ResultSetExtractor<>(rowMapper));
        });
    }

    public <T> Optional<T> queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        List<T> entities = query(sql, rowMapper, args);
        return DataAccessUtils.optionalSingleResult(entities);
    }

    public int update(String sql, Object... args) {
        return execute(sql, statement -> {
            preparedStatementSetter.setParams(statement, args);
            return statement.executeUpdate();
        });
    }

    private <T> List<T> executeQuery(PreparedStatement statement,
                                     ResultSetExtractor<T> resultSetExtractor) throws SQLException {
        try (final var resultSet = statement.executeQuery()) {
            return resultSetExtractor.extractData(resultSet);
        }
    }
}
