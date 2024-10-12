package com.interface21.jdbc.core;

import com.interface21.jdbc.ObjectMapper;
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

    public <T> T query(ObjectMapper<T> objectMapper, String sql, Object... param) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            setParamToStatement(pstmt, param);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return objectMapper.mapToObject(rs);
                }
                throw new IllegalStateException("Fail to get result set"); //TODO: 예외 구체화
            }

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public <T> List<T> queryList(ObjectMapper<T> objectMapper, String sql, Object... param) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            setParamToStatement(pstmt, param);

            List<T> results = new ArrayList<>();
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    results.add(objectMapper.mapToObject(rs));
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
}
