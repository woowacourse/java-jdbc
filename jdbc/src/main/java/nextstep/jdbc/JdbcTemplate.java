package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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

    public void execute(final String sql) {
        execute(sql, PreparedStatement::execute);
    }

    public int update(final String sql, final Object... objects) {
        return execute(sql, objects);
    }

    public <T> T query(final String sql, final RowMapper<T> rowMapper, final Object... objects) {
        return execute(sql, preparedStatement -> getResult(rowMapper, preparedStatement, objects));
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper) {
        return execute(sql, preparedStatement -> getResults(preparedStatement, rowMapper));
    }

    private <T> T execute(final String sql, final PreparedStatementCallback<T> callback) {
        Connection connection = getConnection();
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            return callback.doInPreparedStatement(preparedStatement);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    private int execute(final String sql, final Object[] objects) {
        return execute(sql, preparedStatement -> {
            setValues(preparedStatement, objects);
            return preparedStatement.executeUpdate();
        });
    }

    private void setValues(final PreparedStatement preparedStatement, Object[] objects) throws SQLException {
        PreparedStatementSetter preparedStatementSetter = new ArgumentPreparedStatementSetter(objects);
        preparedStatementSetter.setValues(preparedStatement);
    }

    private <T> T getResult(final RowMapper<T> rowMapper,
                            final PreparedStatement preparedStatement,
                            final Object[] objects) throws SQLException {
        setValues(preparedStatement, objects);
        return DataAccessUtils.singleResult(getResults(preparedStatement, rowMapper));
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

    private Connection getConnection() {
        return DataSourceUtils.getConnection(dataSource);
    }
}
