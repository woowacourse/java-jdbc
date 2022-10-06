package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.annotation.Nullable;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, @Nullable Object... args) {
        final Executor<List<T>> queryExecutor = new QueryExecutor<>(rowMapper);
        return executeQuery(sql, queryExecutor, args);
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, @Nullable Object... args) {
        final List<T> results = query(sql, rowMapper, args);
        return DataAccessUtils.nullableSingleResult(results);
    }

    public int update(final String sql, @Nullable Object... args) {
        final Executor<Integer> queryExecutor = new UpdateExecutor();
        return executeQuery(sql, queryExecutor, args);
    }

    private <T> T executeQuery(final String sql, final Executor<T> queryExecutor, final Object[] args) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            PreparedStatementSetter.setValues(preparedStatement, args);
            return queryExecutor.execute(preparedStatement);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    interface Executor<T> {
        T execute(PreparedStatement preparedStatement) throws SQLException;
    }

    static class QueryExecutor<T> implements Executor<List<T>> {

        private RowMapper<T> rowMapper;

        public QueryExecutor(final RowMapper<T> rowMapper) {
            this.rowMapper = rowMapper;
        }

        @Override
        public List<T> execute(final PreparedStatement preparedStatement) throws SQLException {
            final ResultSet resultSet = preparedStatement.executeQuery();
            final RowMapperResultSetExtractor<T> rowMapperResultSetExtractor = new RowMapperResultSetExtractor<>(rowMapper);
            return rowMapperResultSetExtractor.extractData(resultSet);
        }
    }

    static class UpdateExecutor implements Executor<Integer> {

        @Override
        public Integer execute(final PreparedStatement preparedStatement) throws SQLException {
            return preparedStatement.executeUpdate();
        }
    }
}
