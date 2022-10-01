package nextstep.jdbc;

import java.util.List;
import javax.sql.DataSource;
import nextstep.jdbc.execution.ListExecution;
import nextstep.jdbc.execution.ObjectExecution;
import nextstep.jdbc.execution.UpdateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final JdbcConnector connector;

    public JdbcTemplate(final DataSource dataSource) {
        this.connector = new JdbcConnector(dataSource);
    }

    public void update(String sql, Object[] arguments) {
        connector.execute(new UpdateExecution(sql, arguments));
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper) {
        return connector.execute(new ListExecution<>(sql, rowMapper));
    }

    public <T> T queryForObject(String sql, Object[] arguments, RowMapper<T> rowMapper) {
        return connector.execute(new ObjectExecution<>(sql, arguments, rowMapper));
    }
}
