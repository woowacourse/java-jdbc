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

    public int update(final String sql, final Object... objects) {
        return update(sql, new ArgumentPreparedStatementSetter(objects));
    }

    public int update(final String sql, final PreparedStatementSetter preparedStatementSetter) {
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatementSetter.setValues(preparedStatement);
            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper) {
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            return getResults(rowMapper, preparedStatement.executeQuery());
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private <T> List<T> getResults(final RowMapper<T> rowMapper, final ResultSet resultSet) throws SQLException {
        return results(new RowMapperResultSetExtractor<>(rowMapper), resultSet);
    }
    private <T> List<T> results(final ResultSetExtractor<List<T>> extractor, final ResultSet resultSet) throws SQLException {
        return extractor.extractData(resultSet);
    }

    public <T> T query(final String sql,
                       final RowMapper<T> rowMapper,
                       final PreparedStatementSetter preparedStatementSetter) {
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            ResultSet resultSet = executeQuery(preparedStatement, preparedStatementSetter);
            return DataAccessUtils.singleResult(getResults(rowMapper, resultSet));
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private ResultSet executeQuery(final PreparedStatement preparedStatement,
                                   final PreparedStatementSetter preparedStatementSetter) throws SQLException {
        preparedStatementSetter.setValues(preparedStatement);
        return preparedStatement.executeQuery();
    }

    public void executeUpdate(final PreparedStatementExecutor executor) {
        try (Connection connection = getConnection()) {
            PreparedStatement preparedStatement = executor.execute(connection);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}
