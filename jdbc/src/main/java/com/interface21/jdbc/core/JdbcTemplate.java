package com.interface21.jdbc.core;

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

    public <T> T read(final String sql, RowMapper<T> rowMapper, Object... params){
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = createPreparedStatement(conn, sql, params);
             ResultSet resultSet = pstmt.executeQuery()) {

            if (resultSet.next()) {
                return rowMapper.mapRow(resultSet, 1);
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> List<T> readAll(String sql, RowMapper<T> rowMapper, Object... params) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = createPreparedStatement(conn, sql, params);
             ResultSet resultSet = pstmt.executeQuery()) {

            List<T> results = new ArrayList<>();
            int rowNum = 0;
            while (resultSet.next()) {
                results.add(rowMapper.mapRow(resultSet, rowNum++));
            }
            return results;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int write(final String sql, final Object... params) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = createPreparedStatement(conn, sql, params)) {

            log.debug("query : {}", sql);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private PreparedStatement createPreparedStatement(Connection conn, final String sql, Object... params) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(sql);
        setQueryParams(pstmt, params);
        return pstmt;
    }

    private void setQueryParams(PreparedStatement pstmt, Object... params) throws SQLException {
        int columnIndex = 1;
        for (Object param : params) {
            pstmt.setObject(columnIndex++, param);
        }
    }
}
