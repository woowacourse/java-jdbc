package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import nextstep.jdbc.exception.IncorrectResultSizeDataAccessException;
import nextstep.jdbc.exception.JdbcSqlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> Optional<T> query(final String sql, final RowMapper<T> rowMapper, final Object... arguments) {
        List<T> rows = queryAsList(sql, rowMapper, arguments);

        if (rows.size() > 1) {
            throw new IncorrectResultSizeDataAccessException(sql);
        }

        return rows.stream().findFirst();
    }

    public <T> List<T> queryAsList(final String sql, final RowMapper<T> rowMapper, final Object... arguments) {
        JdbcCallback<List<T>> jdbcCallback = preparedStatement -> {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
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
        return prepareStatementAndThen(sql, PreparedStatement::executeUpdate, arguments);
    }

    public void execute(final String sql, final Object... arguments) {
        prepareStatementAndThen(sql, PreparedStatement::execute, arguments);
    }

    private <T> T prepareStatementAndThen(final String sql, final JdbcCallback<T> jdbcCallback, final Object... arguments) {
        try (Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            bindParameter(preparedStatement, arguments);
            log.debug("query : {}", sql);
            return jdbcCallback.call(preparedStatement);
        } catch (SQLException exception) {
            log.error(exception.getMessage());
            throw new JdbcSqlException("SQL을 실행하는 중에 문제가 발생했습니다.", exception);
        }
    }

    private void bindParameter(final PreparedStatement preparedStatement, final Object... arguments) throws SQLException {
        for (int i = 1; i <= arguments.length; i++) {
            preparedStatement.setObject(i, arguments[i - 1]);
            log.debug("binding parameter [{}] as [{}] - [{}]",
                i,
                preparedStatement.getParameterMetaData().getParameterTypeName(i),
                arguments[i - 1]);
        }
    }
}
