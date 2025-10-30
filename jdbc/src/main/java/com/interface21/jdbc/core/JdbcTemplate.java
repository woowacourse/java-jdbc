package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.dao.IncorrectResultSizeDataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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

    private <T> T execute(String sql, StatementExecutor<T> executor, PreparedStatementSetter pss) {
        Connection conn = null;
        try {
            conn = DataSourceUtils.getConnection(dataSource);
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

                if (pss != null) {
                    pss.setValues(pstmt);
                }
                log.debug("query : {}", sql);

                return executor.execute(pstmt);

            } catch (SQLException e) {
                log.error("SQL execution failed. Query: {}", sql, e);
                throw new DataAccessException(e);
            }
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }

    // INSERT, UPDATE, DELETE
    public int update(String sql, PreparedStatementSetter pss) {
        pss = ensureSetter(pss);
        return execute(sql, PreparedStatement::executeUpdate, pss);
    }

    public int update(String sql, Object... params) {
        return update(sql, PreparedStatementSetter.of(params));
    }

    // SELECT
    public <T> List<T> query(String sql, RowMapper<T> rowMapper, PreparedStatementSetter pss) {
        pss = ensureSetter(pss);
        return execute(sql, pstmt -> {
            try (ResultSet rs = pstmt.executeQuery()) {
                List<T> results = new ArrayList<>();
                addResultSetToResults(rowMapper, rs, results);
                return results;
            }
        }, pss);
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... params) {
        return query(sql, rowMapper, PreparedStatementSetter.of(params));
    }

    // SELECT 단일
    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, PreparedStatementSetter pss) {
        pss = ensureSetter(pss);
        List<T> results = query(sql, rowMapper, pss);
        validateResultsSize(results, 1);
        return results.getFirst();
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... params) {
        return queryForObject(sql, rowMapper, PreparedStatementSetter.of(params));
    }

    private PreparedStatementSetter ensureSetter(PreparedStatementSetter pss) {
        if (pss == null) {
            pss = PreparedStatementSetter.of(null);
        }
        return pss;
    }

    private <T> void validateResultsSize(List<T> results, int expectedSize) {
        int actualSize = results.size();
        if (actualSize != expectedSize) {
            throw new IncorrectResultSizeDataAccessException(expectedSize, actualSize);
        }
    }

    private <T> void addResultSetToResults(RowMapper<T> rowMapper, ResultSet rs, List<T> results) throws SQLException {
        while (rs.next()) {
            results.add(rowMapper.mapRow(rs));
        }
    }
}
