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

    public List<Object> query(String sql, Object... param) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            setParamToStatement(pstmt, param);

            try (ResultSet rs = pstmt.executeQuery()) {
                return mapResultByColumn(rs);
            }

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public List<List<Object>> queryList(String sql, Object... param) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            setParamToStatement(pstmt, param);

            List<List<Object>> results = new ArrayList<>();
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapResultByColumn(rs));
                }
            }
            return results;

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public void execute(String sql, Object... param) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            setParamToStatement(pstmt, param);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private void setParamToStatement(PreparedStatement pstmt, Object[] param) throws SQLException {
        for (int i = 0; i < param.length; i++) {
            pstmt.setObject(i + 1, param[i]);
        }
    }

    private List<Object> mapResultByColumn(ResultSet rs) throws SQLException {
        List<Object> results = new ArrayList<>();
        while (rs.next()) {
            for (int i = 0; i < rs.getMetaData().getColumnCount(); i++) {
                results.add(rs.getObject(i + 1));
            }
        }
        return results;
    }
}
