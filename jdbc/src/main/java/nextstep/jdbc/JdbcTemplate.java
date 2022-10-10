package nextstep.jdbc;

import static nextstep.jdbc.ResultExtractor.extractData;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DataSourceUtils;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> List<T> queryForList(final String sql, final Class<T> targetType, final Object... objects) {
        final StatementCallback<List<T>> statementCallback = statement -> extractData(targetType,
                statement.executeQuery());
        return execute(sql, statementCallback, objects);
    }

    private <T> T execute(final String sql, final StatementCallback<T> statementCallback, final Object... objects) {
        final var connection = DataSourceUtils.getConnection(dataSource);
        try (final var statement = connection.prepareStatement(sql)) {
            StatementSetter.setValues(statement, objects);
            return statementCallback.doInStatement(statement);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e.getMessage(), e);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    public <T> T queryForObject(final String sql, final Class<T> targetType, final Object... objects) {
        final StatementCallback<List<T>> statementCallback = statement -> extractData(targetType,
                statement.executeQuery());
        return JdbcTemplateUtils.singleResult(execute(sql, statementCallback, objects));
    }

    public int update(final String sql, final Object... objects) {
        final StatementCallback<Integer> statementCallback = PreparedStatement::executeUpdate;
        return execute(sql, statementCallback, objects);
    }
}
