package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.support.SQLExceptionTranslator;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(final String sql, Object... args) {
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            log.debug("query : {}", sql);

            ArgumentPreparedStatementSetter pss = new ArgumentPreparedStatementSetter(args);
            pss.setValues(pstmt);

            return pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw SQLExceptionTranslator.translate(sql, e);
        }
    }

    public int update(final Connection conn, final String sql, Object... args) {
        try (
                PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            log.debug("query : {}", sql);

            ArgumentPreparedStatementSetter pss = new ArgumentPreparedStatementSetter(args);
            pss.setValues(pstmt);

            return pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw SQLExceptionTranslator.translate(sql, e);
        }
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, Object... args) {
        log.debug("query : {}", sql);

        PreparedStatementCallback<T> callback = pstmt -> {
            List<T> results = extractResultSet(pstmt.executeQuery(), rowMapper);
            if (results.size() == 1) {
                return results.get(0);
            }
            throw new IncorrectResultSizeDataAccessException(results.size());
        };
        return executeQuery(callback, sql, args);
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, Object... args) {
        PreparedStatementCallback<List<T>> callBack = pstmt ->
                extractResultSet(pstmt.executeQuery(), rowMapper);
        return executeQuery(callBack, sql, args);
    }

    private <T> List<T> extractResultSet(final ResultSet rs, final RowMapper<T> rowMapper) throws SQLException {
        List<T> results = new ArrayList<>();
        int rowNum = 0;
        while (rs.next()) {
            results.add(rowMapper.mapRow(rs, rowNum++));
        }
        return results;
    }

    private <T> T executeQuery(final PreparedStatementCallback<T> action, final String sql, Object... args) {
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            ArgumentPreparedStatementSetter pss = new ArgumentPreparedStatementSetter(args);
            pss.setValues(pstmt);
            return action.doInPreparedStatement(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw SQLExceptionTranslator.translate(sql, e);
        }
    }
}
