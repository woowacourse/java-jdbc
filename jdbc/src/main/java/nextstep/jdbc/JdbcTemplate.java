package nextstep.jdbc;

import java.util.List;
import javax.sql.DataSource;
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
        if (results.size() != 1) {
            throw new DataAccessException("The result of query isn't single. count : " + results.size());
        }
        return results.get(SINGLE_RESULT - 1);
    }
}
