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
    private final SQLExceptionTranslator sqlExceptionTranslator;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
        this.sqlExceptionTranslator = new SQLExceptionTranslator();
    }

    public int update(String sql, Object... args) {
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
            throw sqlExceptionTranslator.translate(sql, e);
        }
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            log.debug("query : {}", sql);

            ArgumentPreparedStatementSetter pss = new ArgumentPreparedStatementSetter(args);
            pss.setValues(pstmt);
            List<T> results = extractResultSet(pstmt.executeQuery(), rowMapper);

            if (results.size() == 1) {
                return results.get(0);
            }
            throw new IncorrectResultSizeDataAccessException(results.size());
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw sqlExceptionTranslator.translate(sql, e);
        }
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper) {
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            return extractResultSet(pstmt.executeQuery(), rowMapper);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw sqlExceptionTranslator.translate(sql, e);
        }
    }

    private <T> List<T> extractResultSet(ResultSet rs, RowMapper<T> rowMapper) throws SQLException {
        List<T> results = new ArrayList<>();
        int rowNum = 0;
        while (rs.next()) {
            results.add(rowMapper.mapRow(rs, rowNum++));
        }
        return results;
    }
}
