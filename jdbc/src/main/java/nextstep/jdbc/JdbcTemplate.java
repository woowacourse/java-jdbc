package nextstep.jdbc;

import java.sql.Connection;
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

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(String sql, Object... parameters) throws DataAccessException {
        return executeUpdate(sql, new ParametersSetter(parameters));
    }

    private int executeUpdate(String sql, PreparedStatementSetter pss) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            pss.setValues(preparedStatement);
            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new BadExecuteUpdateException(e.getMessage());
        }
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... parameters) throws DataAccessException {
        List<T> results = query(sql, rowMapper, parameters);
        validateOneResult(results);
        return results.get(0);
    }

    private <T> void validateOneResult(List<T> results) {
        if (results.size() != 1) {
            throw new QueryForObjectResultSizeException();
        }
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... parameters) throws DataAccessException {
        return executeQuery(sql,  new QueryResults<>(rowMapper), new ParametersSetter(parameters));
    }

    private <T> List<T> executeQuery(String sql, ResultsSetMapper<T> resultsSetMapper, PreparedStatementSetter pss) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            pss.setValues(preparedStatement);
            return resultsSetMapper.collect(preparedStatement.executeQuery());
        } catch (SQLException e) {
            throw new BadExecuteQueryException(e.getMessage());
        }
    }
}
