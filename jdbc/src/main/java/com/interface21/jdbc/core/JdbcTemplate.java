package com.interface21.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... values) {
        return executeQuery(sql, rs -> {
            if (rs.next()) {
                return rowMapper.mapRow(rs);
            }
            return null;
        }, values);
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... values) {
        return executeQuery(sql, rs -> {
            List<T> result = new ArrayList<>();
            while (rs.next()) {
                result.add(rowMapper.mapRow(rs));
            }
            return result;
        }, values);
    }

    private <T> T executeQuery(String sql, ResultSetExtractor<T> resultSetExtractor, Object... values) {
        ResultSet rs = null;

        try (Connection conn = getConnection();
             PreparedStatement pstmt = getPreparedStatement(sql, conn)) {
            assignSqlValues(values, pstmt);
            rs = pstmt.executeQuery();

            log.debug("query : {}", sql);
            return resultSetExtractor.extractData(rs);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            closeResultSet(rs);
        }
    }

    public void update(String sql, Object... values) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = getPreparedStatement(sql, conn)) {
            log.debug("query : {}", sql);

            assignSqlValues(values, pstmt);

            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    private PreparedStatement getPreparedStatement(String sql, Connection conn) throws SQLException {
        return conn.prepareStatement(sql);
    }

    private void assignSqlValues(Object[] values, PreparedStatement pstmt) throws SQLException {
        for (int i = 1; i <= values.length; i++) {
            pstmt.setObject(i, values[i - 1]);
        }
    }

    private void closeResultSet(ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException ignored) {
        }
    }
}
