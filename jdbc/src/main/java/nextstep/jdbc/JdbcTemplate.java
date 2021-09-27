package nextstep.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import nextstep.jdbc.exception.IncorrectResultSizeDataAccessException;
import nextstep.jdbc.exception.JdbcNotFoundException;
import nextstep.jdbc.exception.JdbcSqlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T query(final String sql, final RowMapper<T> rowMapper, final Object... arguments) {
        List<T> rows = queryAsList(sql, rowMapper, arguments);

        if (rows.isEmpty()) {
            throw new JdbcNotFoundException(sql);
        }
        if (rows.size() > 1) {
            throw new IncorrectResultSizeDataAccessException(sql);
        }

        return rows.get(0);
    }

    public <T> List<T> queryAsList(final String sql, final RowMapper<T> rowMapper, final Object... arguments) {
        JdbcCallback<List<T>> jdbcCallback = statement -> {
            try (ResultSet resultSet = statement.preparedStatementValue().executeQuery()) {
                List<T> results = new ArrayList<>();
                while (resultSet.next()) {
                    results.add(rowMapper.map(resultSet));
                }
                return results;
            }
        };

        return prepareStatementAndThen(sql, jdbcCallback, arguments);
    }

    public int update(final String sql, final Object... arguments) {
        return prepareStatementAndThen(sql, statement -> statement.preparedStatementValue().executeUpdate(), arguments);
    }

    public void execute(final String sql, final Object... arguments) {
        prepareStatementAndThen(sql, statement -> statement.preparedStatementValue().execute(), arguments);
    }

    private <T> T prepareStatementAndThen(final String sql, final JdbcCallback<T> jdbcCallback, final Object... arguments) {
        try (Connection connection = dataSource.getConnection();
            ParameterizedStatement statement = ParameterizedStatement.from(connection, sql, arguments)) {
            log.debug("query : {}", sql);
            return jdbcCallback.call(statement);
        } catch (SQLException exception) {
            log.error(exception.getMessage());
            throw new JdbcSqlException("SQL을 실행하는 중에 문제가 발생했습니다.", exception);
        }
    }
}
