package org.springframework.jdbc.core;

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
    private static final String QUERY_LOG = "query : {}";

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T queryForObject(String sql, Class<T> type, Object... args) {
        try (Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)
        ) {
            log.debug(QUERY_LOG, sql);
            setArgs(pstmt, sql, args);
            ResultSet resultSet = pstmt.executeQuery();
            validOneResult(resultSet);
            return ObjectConverter.convertForObject(resultSet, type);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private void validOneResult(ResultSet resultSet) throws SQLException {
        if(!resultSet.next()){
            throw new IncorrectResultSizeDataAccessException("No rows selected");
        }
        if (resultSet.next()) {
            throw new IncorrectResultSizeDataAccessException("More than one row selected");
        }
        resultSet.beforeFirst();
    }

    private void setArgs(PreparedStatement pstmt, String sql, Object[] args) throws SQLException {
        long sqlArgsCount = countArgs(sql, '?');
        int providedArgsCount = args.length;
        validateArgs(sqlArgsCount, providedArgsCount);
        int argumentPoint = 1;
        for (Object arg : args) {
            pstmt.setObject(argumentPoint, arg);
            argumentPoint++;
        }
    }

    private long countArgs(String sql, char arg) {
        return sql.chars()
            .filter(c -> c == arg)
            .count();
    }

    private void validateArgs(long sqlArgsCount, int providedArgsCount) {
        if (sqlArgsCount != providedArgsCount) {
            throw new InvalidArgsException(String.format("Invalid Argument count required = %d but provided %d",
                sqlArgsCount,
                providedArgsCount));
        }
    }

    public <T> List<T> queryForList(String sql, Class<T> type, Object... args) {
        try (Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            log.debug(QUERY_LOG, sql);
            setArgs(pstmt, sql, args);
            return ObjectConverter.convertForList(pstmt.executeQuery(), type);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public int update(String sql, Object... args) {
        try (Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            log.debug(QUERY_LOG, sql);
            setArgs(pstmt, sql, args);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
