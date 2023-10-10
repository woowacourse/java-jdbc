package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.SQLExceptionTranslator;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(final String sql, final boolean closeResources, @Nullable Object... args) {
        PreparedStatementCallback<Integer> action = (pstmt) -> {
            ArgumentPreparedStatementSetter pss = new ArgumentPreparedStatementSetter(args);
            pss.setValues(pstmt);
            return pstmt.executeUpdate();
        };
        return executeQuery(action, sql, closeResources, args);

    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final boolean closeResources, Object... args) {
        log.debug("query : {}", sql);

        PreparedStatementCallback<T> callback = pstmt -> {
            ArgumentPreparedStatementSetter pss = new ArgumentPreparedStatementSetter(args);
            pss.setValues(pstmt);
            List<T> results = extractResultSet(pstmt.executeQuery(), rowMapper);
            if (results.size() == 1) {
                return results.get(0);
            }
            throw new IncorrectResultSizeDataAccessException(results.size());
        };
        return executeQuery(callback, sql, closeResources, args);
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final boolean closeResource, @Nullable Object... args) {
        PreparedStatementCallback<List<T>> callBack = pstmt -> {
            ArgumentPreparedStatementSetter pss = new ArgumentPreparedStatementSetter(args);
            pss.setValues(pstmt);
            return extractResultSet(pstmt.executeQuery(), rowMapper);
        };
        return executeQuery(callBack, sql, closeResource, args);
    }

    private <T> List<T> extractResultSet(final ResultSet rs, final RowMapper<T> rowMapper) throws SQLException {
        List<T> results = new ArrayList<>();
        int rowNum = 0;
        while (rs.next()) {
            results.add(rowMapper.mapRow(rs, rowNum++));
        }
        return results;
    }

    private <T> T executeQuery(final PreparedStatementCallback<T> action, final String sql, final boolean closeResources, @Nullable Object... args) {
        Connection conn = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);

            return action.doInPreparedStatement(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw SQLExceptionTranslator.translate(sql, e);
        } finally {
            if (closeResources) {
                DataSourceUtils.releaseConnection(dataSource);
            }
        }
    }
}
