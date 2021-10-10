package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import javax.sql.DataSource;
import nextstep.jdbc.exception.DataAccessException;
import nextstep.jdbc.exception.DatabaseConnectionFailureException;
import nextstep.jdbc.exception.InvalidSQLMethodException;
import nextstep.jdbc.exception.KeyGenerationFailureException;
import nextstep.jdbc.exception.PreparedStatementCreationFailureException;
import nextstep.jdbc.exception.QueryExecutionFailureException;
import nextstep.jdbc.exception.ResultSizeEmptyException;
import nextstep.jdbc.exception.ResultSizeOverflowException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);
    private static final String SQL_INFO_LOG = "query : {}";
    private static final String INSERT_METHOD = "insert";
    private static final String DELETE_METHOD = "delete";

    private final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(String sql, Object... args) {
        return execute(sql, pstmt -> {
            PreparedStatementValueSetter valueSetter = new PreparedStatementValueSetter(pstmt);
            valueSetter.setPreparedStatementValues(args);
            return pstmt.executeUpdate();
        });
    }

    public <T> T query(String sql, RowMapper<T> rowMapper, Object... args) {
        return execute(sql, pstmt -> {
            PreparedStatementValueSetter valueSetter = new PreparedStatementValueSetter(pstmt);
            valueSetter.setPreparedStatementValues(args);
            return executeQueryForObject(pstmt, rowMapper);
        });
    }

    private <T> void validateSingleResult(List<T> results) {
        if (results.isEmpty()) {
            throw new ResultSizeEmptyException();
        }
        if (results.size() > 1) {
            throw new ResultSizeOverflowException(results.size());
        }
    }

    public <T> List<T> queryForList(String sql, RowMapper<T> rowMapper) {
        return execute(sql, pstmt -> executeQuery(pstmt, rowMapper));
    }

    public <T> T insert(String sql, Class<T> keyType, Object... args) {
        if (notMethod(sql, INSERT_METHOD)) {
            throw new InvalidSQLMethodException(INSERT_METHOD, sql);
        }
        return execute(sql, pstmt -> {
            PreparedStatementValueSetter valueSetter = new PreparedStatementValueSetter(pstmt);
            valueSetter.setPreparedStatementValues(args);
            pstmt.executeUpdate();
            return generatedKey(pstmt, keyType);
        });
    }

    private boolean notMethod(String sql, String method) {
        return !sql.startsWith(method);
    }

    private <T> T generatedKey(PreparedStatement pstmt, Class<T> keyType) {
        try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
            if (generatedKeys.next()) {
                return generatedKeys.getObject(1, keyType);
            }
            return null;
        } catch (SQLException exception) {
            throw new KeyGenerationFailureException(exception);
        }
    }

    public int delete(String sql, Long id) {
        if (notMethod(sql, DELETE_METHOD)) {
            throw new InvalidSQLMethodException(DELETE_METHOD, sql);
        }
        return execute(sql, pstmt -> {
            PreparedStatementValueSetter valueSetter = new PreparedStatementValueSetter(pstmt);
            valueSetter.setPreparedStatementValue(id);
            return pstmt.executeUpdate();
        });
    }

    private <T> T execute(String sql, PreparedStatementExecutor<T> executor) {
        try (
            Connection conn = getConnection();
            PreparedStatement pstmt = getPreparedStatement(conn, sql)
        ) {
            if (log.isDebugEnabled()) {
                log.debug(SQL_INFO_LOG, sql);
            }
            return executor.execute(pstmt);
        } catch (SQLException exception) {
            throw new DataAccessException(exception);
        }
    }

    private <T> T executeQueryForObject(PreparedStatement pstmt, RowMapper<T> rowMapper) {
        List<T> results = executeQuery(pstmt, rowMapper);
        validateSingleResult(results);
        return results.get(0);
    }

    private <T> List<T> executeQuery(PreparedStatement pstmt, RowMapper<T> rowMapper) {
        try (ResultSet resultSet = pstmt.executeQuery()) {
            ResultSetExtractor<T> resultSetExtractor = new ResultSetExtractor<>(resultSet, rowMapper);
            return resultSetExtractor.toList();
        } catch (SQLException exception) {
            throw new QueryExecutionFailureException(exception);
        }
    }

    private Connection getConnection() throws DatabaseConnectionFailureException{
        try {
            return dataSource.getConnection();
        } catch (SQLException exception) {
            throw new DatabaseConnectionFailureException(exception);
        }
    }

    private PreparedStatement getPreparedStatement(Connection conn, String sql) {
        try {
            return conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        } catch (SQLException exception) {
            throw new PreparedStatementCreationFailureException(exception);
        }
    }
}
