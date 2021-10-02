package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import nextstep.jdbc.exception.DataAccessException;
import nextstep.jdbc.exception.DatabaseConnectionFailureException;
import nextstep.jdbc.exception.ResultSizeEmptyException;
import nextstep.jdbc.exception.ResultSizeOverflowException;
import nextstep.jdbc.exception.PreparedStatementCreationFailureException;
import nextstep.jdbc.exception.QueryExecutionFailureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);
    public static final String SQL_INFO_LOG = "query : {}";

    private final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // TODO : make usage of this int
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
            return getObjectResult(pstmt, rowMapper);
        });
    }

    public <T> List<T> queryForList(String sql, RowMapper<T> rowMapper) {
        return execute(sql, pstmt -> getListResult(pstmt, rowMapper));
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

    private <T> T getObjectResult(PreparedStatement pstmt, RowMapper<T> rowMapper) {
        List<T> results = executeQuery(pstmt, rowMapper);
        validateSingleResult(results);
        return results.get(0);
    }

    private <T> List<T> getListResult(PreparedStatement pstmt, RowMapper<T> rowMapper) {
        return executeQuery(pstmt, rowMapper);
    }

    private <T> List<T> executeQuery(PreparedStatement pstmt, RowMapper<T> rowMapper) {
        try (ResultSet resultSet = pstmt.executeQuery()) {
            ResultSetExtractor<T> resultSetExtractor = new ResultSetExtractor<>(resultSet, rowMapper);
            return resultSetExtractor.toList();
        } catch (SQLException exception) {
            throw new QueryExecutionFailureException(exception);
        }
    }

    private <T> void validateSingleResult(List<T> results) {
        if (results.isEmpty()) {
            throw new ResultSizeEmptyException();
        }
        if (results.size() > 1) {
            throw new ResultSizeOverflowException(results.size());
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
            return conn.prepareStatement(sql);
        } catch (SQLException exception) {
            throw new PreparedStatementCreationFailureException(exception);
        }
    }

    public void delete(String sql, Long id) {
        execute(sql, pstmt -> {
            PreparedStatementValueSetter valueSetter = new PreparedStatementValueSetter(pstmt);
            valueSetter.setPreparedStatementValue(id);
            return pstmt.executeUpdate();
        });
    }
}
