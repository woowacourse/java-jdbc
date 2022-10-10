package nextstep.jdbc;

import java.util.List;
import javax.sql.DataSource;
import nextstep.jdbc.exception.EmptyResultDataAccessException;
import nextstep.jdbc.exception.IncorrectResultSizeDataAccessException;
import nextstep.jdbc.execution.QueryExecution;
import nextstep.jdbc.execution.UpdateExecution;

public class JdbcTemplate {

    private static final int SINGLE_RESULT = 1;

    private final JdbcConnector connector;

    public JdbcTemplate(final DataSource dataSource) {
        this.connector = new JdbcConnector(dataSource);
    }

    public void update(String sql, Object... arguments) {
        connector.execute(new UpdateExecution(sql, arguments));
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper) {
        return connector.execute(new QueryExecution<>(sql, rowMapper));
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... arguments) {
        List<T> results = connector.execute(new QueryExecution<>(sql, rowMapper, arguments));
        if (results.size() == 0) {
            throw new EmptyResultDataAccessException("Failed to find result.");
        }
        if (results.size() > 1) {
            throw new IncorrectResultSizeDataAccessException("The result of query isn't single. " + results.size());
        }
        return results.get(SINGLE_RESULT - 1);
    }
}
