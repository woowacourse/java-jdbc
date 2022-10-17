package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DataSourceUtils;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);
    private static final int INITIAL_PARAM_INDEX = 1;
    private static final int SINGLE_RESULT_SIZE = 1;

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(final String sql, final Object... params) {
        runContext(sql, statement -> {
            setParams(statement, params);
            return statement.executeUpdate();
        });
    }

    private <T> T runContext(final String sql, JdbcAction<T> action) {
        final Connection connection = DataSourceUtils.getConnection(dataSource);
        try (final PreparedStatement statement = connection.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            return action.doAction(statement);
        } catch (SQLException exception) {
            log.warn("SQL Exception alert!!! : {}", exception.getMessage(), exception);
            throw new DataAccessException();
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    private void setParams(final PreparedStatement statement, final Object... params) throws SQLException {
        int paramIndex = INITIAL_PARAM_INDEX;
        for (Object param : params) {
            statement.setObject(paramIndex, param);
            paramIndex++;
        }
    }

    public <T> List<T> find(final String sql, RowMapper<T> rowMapper, Object... params) {
        return runContext(sql, statement -> {
            setParams(statement, params);
            final ResultSet resultSet = statement.executeQuery();
            return mapToList(resultSet, rowMapper);
        });
    }

    private <T> List<T> mapToList(final ResultSet resultSet, final RowMapper<T> rowMapper) throws SQLException {
        List<T> result = new ArrayList<>();
        while (resultSet.next()) {
            final T mappedValue = rowMapper.mapRow(resultSet);
            result.add(mappedValue);
        }
        resultSet.close();
        return result;
    }

    public <T> T findSingleResult(final String sql, RowMapper<T> rowMapper, Object... params) {
        final List<T> results = find(sql, rowMapper, params);
        validateSingleResult(results);
        return results.iterator().next();
    }

    private <T> void validateSingleResult(final List<T> results) {
        final int size = results.size();
        if (size != SINGLE_RESULT_SIZE) {
            log.warn("결과 값이 한 개가 아닙니다. 결과 값 = {}", size);
            throw new DataAccessException();
        }
    }
}
