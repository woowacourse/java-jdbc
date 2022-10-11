package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DataSourceUtils;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);
    private static final String DATA_EMPTY_ERROR_MESSAGE = "데이터가 없습니다.";
    private static final String DATA_NOT_SINGLE_ERROR_MESSAGE = "데이터가 한개가 아닙니다. 데이터 수 : %d";

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(final String sql, final Object... parameters) {
        execute(sql, statement -> statement.executeUpdate(), parameters);
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... parameters) {
        return execute(sql, statement -> executeQuery(statement, rowMapper), parameters);
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... parameters) {
        return execute(sql, statement -> getSingleRow(executeQuery(statement, rowMapper)), parameters);
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    private <T> T execute(final String sql, final Executor<T> executor, final Object[] parameters) {
        final Connection conn = DataSourceUtils.getConnection(dataSource);
        try (final PreparedStatement statement = PreparedStatementFactory.create(conn, sql, parameters)) {
            log.debug("query : {}", sql);

            return executor.execute(statement);
        } catch (final SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }

    private <T> List<T> executeQuery(final PreparedStatement statement, final RowMapper<T> rowMapper)
            throws SQLException {
        final List<T> results = new ArrayList<>();

        final ResultSet rs = statement.executeQuery();
        while (rs.next()) {
            final T row = rowMapper.mapRow(rs);
            results.add(row);
        }

        return results;
    }

    private <T> T getSingleRow(final List<T> results) {
        validateExecuteResultSize(results);

        return results.get(0);
    }

    private <T> void validateExecuteResultSize(final List<T> results) {
        if (results == null || results.isEmpty()) {
            log.error(DATA_EMPTY_ERROR_MESSAGE);
            throw new DataAccessException(DATA_EMPTY_ERROR_MESSAGE);
        }
        if (results.size() >= 2) {
            final String message = String.format(DATA_NOT_SINGLE_ERROR_MESSAGE, results.size());
            log.error(message);
            throw new DataAccessException(message);
        }
    }
}
