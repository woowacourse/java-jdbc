package com.interface21.jdbc.core;

import com.interface21.jdbc.mapper.RowMapper;
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
    private final RowMapper rowMapper;

    public JdbcTemplate(final DataSource dataSource, final RowMapper rowMapper) {
        this.dataSource = dataSource;
        this.rowMapper = rowMapper;
    }

    public void executeUpdate(String sql, Object[] params) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            setParams(pstmt, params);

            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T executeSelect(String sql, Object[] params, Class<T> clazz) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            setParams(pstmt, params);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rowMapper.map(rs, clazz);
            }
            return null;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public <T> List<T> executeSelectAll(String sql, Object[] params, Class<T> clazz) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            setParams(pstmt, params);
            ResultSet rs = pstmt.executeQuery();

            List<T> results = new ArrayList<>();

            while (rs.next()) {
                results.add(rowMapper.map(rs, clazz));
            }
            return results;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setParams(PreparedStatement pstmt, Object[] params) throws SQLException {
        if (params == null || params.length == 0) {
            return;
        }

        for (int i = 0; i < params.length; i++) {
            pstmt.setObject(i + 1, params[i]);
        }
    }
}
