package nextstep.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import org.springframework.jdbc.core.SingleColumnRowMapper;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private <T> T execute(final StatementCallback statementCallback, final Object[] objects,
                          final ResultSetExtractor<T> resultSetExtractor) {
        statementCallback.setPreparedSql(objects);
        return statementCallback.doInStatement(resultSetExtractor);
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... objects)
            throws DataAccessException {
        try (Connection connection = dataSource.getConnection();
             StatementCallback statementCallback = new StatementCallback(connection.prepareStatement(sql))) {
            return execute(statementCallback, objects, new RowMapperResultSetExtractor<>(rowMapper));
        } catch (SQLException e) {
            log.error("query exception", e);
            throw new DataAccessException();
        }
    }

    public <T> List<T> query(final String sql, final Class<T> cls, Object... objects) throws DataAccessException {
        return query(sql, new SingleColumnRowMapper<>(cls), objects);
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, Object... objects)
            throws DataAccessException {
        try (Connection connection = dataSource.getConnection();
             StatementCallback statementCallback = new StatementCallback(connection.prepareStatement(sql))) {
            return DataAccessUtils.nullableSingleResult(
                    execute(statementCallback, objects, new RowMapperResultSetExtractor<>(rowMapper)));
        } catch (SQLException e) {
            log.error("queryForObject exception", e);
            throw new DataAccessException();
        }
    }

    public <T> T queryForObject(final String sql, final Class<T> cls, Object... objects) throws DataAccessException {
        return queryForObject(sql, new SingleColumnRowMapper<>(cls), objects);
    }
}
