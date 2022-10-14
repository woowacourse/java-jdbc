package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import nextstep.jdbc.exception.DataAccessException;
import nextstep.jdbc.exception.InvalidDataSizeException;
import nextstep.jdbc.exception.InvalidSqlException;
import nextstep.jdbc.exception.NoSuchDataException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> List<T> query(final String sql, final ObjectMapper<T> objectMapper) {
        return executeQuery(sql, statement -> {
            log.debug("query : {}", sql);
            return executeStatement(statement, objectMapper);
        });
    }

    public <T> T queryForObject(final String sql, final Object parameter, final ObjectMapper<T> objectMapper) {
        validateSql(sql, parameter);
        return executeQuery(sql, statement -> {
            statement.setObject(1, parameter);
            List<T> results = executeStatement(statement, objectMapper);
            return getSingleResult(results);
        });
    }

    public void update(final String sql, final Object... parameters) {
        validateSql(sql, parameters);
        executeQuery(sql, statement -> {
            setParams(statement, parameters);
            log.debug("query : {}", sql);
            return statement.executeUpdate();
        });
    }

    public void update(final Connection connection, final String sql, final Object... parameters) {
        validateSql(sql, parameters);
        executeQuery(connection, sql, statement -> {
            setParams(statement, parameters);
            log.debug("query : {}", sql);
            return statement.executeUpdate();
        });
    }

    private <T> T executeQuery(final Connection connection, final String sql, final QueryExecutor<T> queryExecutor) {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            return queryExecutor.execute(statement);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    private <T> T executeQuery(final String sql, final QueryExecutor<T> queryExecutor) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            return queryExecutor.execute(statement);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    private <T> List<T> executeStatement(final PreparedStatement statement, final ObjectMapper<T> objectMapper) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery()) {
            return mappingRows(resultSet, objectMapper);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    private <T> List<T> mappingRows(final ResultSet resultSet, final ObjectMapper<T> objectMapper) throws SQLException {
        List<T> results = new ArrayList<>();
        while (resultSet.next()) {
            T t = objectMapper.map(resultSet);
            results.add(t);
        }
        return results;
    }

    private <T> T getSingleResult(final List<T> results) {
        if (results.isEmpty()) {
            throw new NoSuchDataException("조회 결과가 존재하지 않습니다.");
        }
        if (results.size() > 1) {
            throw new InvalidDataSizeException(1, results.size());
        }
        return results.get(0);
    }

    private void setParams(final PreparedStatement statement, final Object[] parameters) throws SQLException {
        for (int i = 0; i < parameters.length; i++) {
            statement.setObject(i + 1, parameters[i]);
        }
    }

    private void validateSql(final String sql, final Object... parameters) {
        int required = sql.length() - sql.replaceAll("\\?", "").length();
        if (required != parameters.length) {
            throw new InvalidSqlException("sql문 내의 매개변수와 주어진 매개변수의 수가 다릅니다.");
        }
    }
}
