package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import org.springframework.jdbc.datasource.DataSourceUtils;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> Optional<T> queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        List<T> entities = query(sql, rowMapper, args);
        if (entities.size() > 1) {
            throw new DataAccessException("조회된 데이터의 개수가 1개를 초과합니다.");
        }
        if (entities.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(entities.get(0));
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) {
        return execute(sql, statement -> {
            PreparedStatementSetter.setParams(statement, args);
            return executeQuery(statement, new ResultSetExtractor<>(rowMapper));
        });
    }

    public int update(String sql, Object... args) {
        return execute(sql, statement -> {
            PreparedStatementSetter.setParams(statement, args);
            return statement.executeUpdate();
        });
    }

    private <T> T execute(String sql, PreparedStatementCallback<T> statementCallback) {
        final var connection = DataSourceUtils.getConnection(dataSource);
        try (final var preparedStatement = connection.prepareStatement(sql)) {
            return statementCallback.execute(preparedStatement);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private <T> List<T> executeQuery(PreparedStatement statement,
                                     ResultSetExtractor<T> resultSetExtractor) throws SQLException {
        try (final var resultSet = statement.executeQuery()) {
            return resultSetExtractor.extractData(resultSet);
        }
    }
}
