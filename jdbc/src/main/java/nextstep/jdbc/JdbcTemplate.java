package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
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

    public int update(final String sql, final Object... parameters) throws DataAccessException {
        return executeUpdate(sql, new ParametersSetter(parameters));
    }

    private int executeUpdate(String sql, PreparedStatementSetter pss) {
        try (Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            pss.setValues(preparedStatement);
            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, Object... parameters) throws DataAccessException {
        List<T> results = query(sql, rowMapper, parameters);
        validateOneResult(results);
        return results.get(0);
    }

    private <T> void validateOneResult(List<T> results) {
        if (results.size() != 1) {
            throw new DataAccessException();
        }
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper,
                             final Object... parameters) throws DataAccessException {
        return executeQuery(sql,  new QueryResults<>(rowMapper), new ParametersSetter(parameters));
    }

    private <T> List<T> executeQuery(String sql, ResultsSetMapper<T> resultsSetMapper,
                                     PreparedStatementSetter pss) {
        try (Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            pss.setValues(preparedStatement);
            return resultsSetMapper.collect(preparedStatement.executeQuery());
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }
}
