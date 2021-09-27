package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import nextstep.jdbc.exception.DataAccessException;
import nextstep.jdbc.exception.DatabaseConnectionFailureException;
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

    public int update(String sql, PreparedStatementSetter pss) {
        return execute(sql, pstmt -> {
            PreparedStatementValueSetter valueSetter = new PreparedStatementValueSetter(pstmt);
            valueSetter.setPreparedStatementValues(pss);
            return pstmt.executeUpdate();
        });
    }

    public int update(String sql, Object... args) {
        return execute(sql, pstmt -> {
            PreparedStatementValueSetter valueSetter = new PreparedStatementValueSetter(pstmt);
            valueSetter.setPreparedStatementValues(args);
            return pstmt.executeUpdate();
        });
    }

    public int update(String sql, Object[] args, int[] argTypes) {
        return execute(sql, pstmt -> {
            PreparedStatementValueSetter valueSetter = new PreparedStatementValueSetter(pstmt);
            valueSetter.setPreparedStatementValues(args, argTypes);
            return pstmt.executeUpdate();
        });
    }

    public <T> T query(String sql, RowMapper<T> rowMapper, PreparedStatementSetter setter) {
        return execute(sql, pstmt -> {
            PreparedStatementValueSetter valueSetter = new PreparedStatementValueSetter(pstmt);
            valueSetter.setPreparedStatementValues(setter);
            return getObjectResult(rowMapper, pstmt);
        });
    }

    public <T> T query(String sql, RowMapper<T> rowMapper, Object... args) {
        return execute(sql, pstmt -> {
            PreparedStatementValueSetter valueSetter = new PreparedStatementValueSetter(pstmt);
            valueSetter.setPreparedStatementValues(args);
            return getObjectResult(rowMapper, pstmt);
        });
    }

    public <T> List<T> queryForList(String sql, RowMapper<T> rowMapper) {
        return execute(sql, pstmt -> getListResult(rowMapper, pstmt));
    }

    private <T> List<T> getListResult(RowMapper<T> rowMapper, PreparedStatement pstmt) {
        ResultSet resultSet = getResultSet(pstmt);
        ResultSetExtractor<T> resultSetExtractor = new ResultSetExtractor<>(rowMapper);
        return resultSetExtractor.toList(resultSet);
    }

    private <T> T getObjectResult(RowMapper<T> rowMapper, PreparedStatement pstmt) {
        ResultSet resultSet = getResultSet(pstmt);
        ResultSetExtractor<T> resultSetExtractor = new ResultSetExtractor<>(rowMapper);
        return resultSetExtractor.toObject(resultSet);
    }

    private ResultSet getResultSet(PreparedStatement pstmt) {
        try {
            return pstmt.executeQuery();
        } catch (SQLException e) {
            throw new QueryExecutionFailureException(e.getMessage(), e.getCause());
        }
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

    private Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new DatabaseConnectionFailureException(e.getMessage(), e.getCause());
        }
    }

    private PreparedStatement getPreparedStatement(Connection conn, String sql) {
        try {
            return conn.prepareStatement(sql);
        } catch (SQLException e) {
            throw new PreparedStatementCreationFailureException(e.getMessage(), e.getCause());
        }
    }
}
