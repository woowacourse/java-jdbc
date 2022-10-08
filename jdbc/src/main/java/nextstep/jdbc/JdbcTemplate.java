package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void execute(final String sql) {
        execute(sql, PreparedStatement::execute);
    }

    public int update(final String sql, final Object... objects) {
        return update(sql, new ArgumentPreparedStatementSetter(objects));
    }

    public <T> T query(final String sql,
                       final RowMapper<T> rowMapper,
                       final PreparedStatementSetter preparedStatementSetter) {
        return execute(sql, preparedStatement -> {
            preparedStatementSetter.setValues(preparedStatement);
            return DataAccessUtils.singleResult(getResults(preparedStatement, rowMapper));
        });
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper) {
        return execute(sql, preparedStatement -> getResults(preparedStatement, rowMapper));
    }

    private <T> T execute(final String sql, final PreparedStatementCallback<T> callback) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            return callback.doInPreparedStatement(preparedStatement);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private int update(final String sql, final PreparedStatementSetter preparedStatementSetter) {
        return execute(sql, preparedStatement -> {
            preparedStatementSetter.setValues(preparedStatement);
            return preparedStatement.executeUpdate();
        });
    }

    private <T> List<T> getResults(final PreparedStatement preparedStatement, final RowMapper<T> rowMapper) {
        try (ResultSet resultSet = preparedStatement.executeQuery()) {
            return results(new RowMapperResultSetExtractor<>(rowMapper), resultSet);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private <T> List<T> results(final ResultSetExtractor<List<T>> extractor,
                                final ResultSet resultSet) throws SQLException {
        return extractor.extractData(resultSet);
    }
}
