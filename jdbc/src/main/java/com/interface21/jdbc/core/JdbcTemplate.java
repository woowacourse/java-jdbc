package com.interface21.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public DataSource getDataSource(){
        return this.dataSource;
    }

    public List<Map<String, Object>> queryForResultList(String sql) {
        final List<Map<String, Object>> results = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            try (ResultSet rs = pstmt.executeQuery()) {
                final var metaData = rs.getMetaData();
                final int columnCount = metaData.getColumnCount();

                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = metaData.getColumnName(i);
                        Object value = rs.getObject(i);
                        row.put(columnName, value);
                    }
                    results.add(row);
                }
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
        return results;
    }

    public Optional<Map<String, Object>> queryForResult(String sql, Object... args) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            setParameter(args, pstmt);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    final var metaData = rs.getMetaData();
                    final int columnCount = metaData.getColumnCount();
                    Map<String, Object> row = new HashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = metaData.getColumnName(i);
                        Object value = rs.getObject(i);
                        row.put(columnName, value);
                    }
                    return Optional.of(row);
                }
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    public void queryForUpdate(final String sql, final Object...args) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            setParameter(args, pstmt);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private void setParameter(Object[] args, PreparedStatement pstmt) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            pstmt.setObject(i+1, args[i]);
        }
    }
}
