package com.interface21.jdbc.core;

import java.sql.Connection;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(String sql, Object ... objects) {
        try (Connection conn = dataSource.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            validateParameterCount(objects, pstmt);
            setParameter(objects, pstmt);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object ...objects) {
        List<T> query = query(sql, rowMapper, objects);
        return query.getFirst();
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object ...objects) {
        ResultSet rs = null;
        try (Connection conn = dataSource.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            validateParameterCount(objects, pstmt);
            setParameter(objects, pstmt);
            rs = pstmt.executeQuery();
            return getQueryResult(rowMapper, rs);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new IllegalArgumentException(e.getMessage(), e);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException ignored) {}
        }
    }

    private static <T> List<T> getQueryResult(RowMapper<T> rowMapper, ResultSet rs) throws SQLException {
        List<T> re = new ArrayList<>();
        while (rs.next()) {
            re.add(rowMapper.mapRow(rs));
        }
        return re;
    }

    private static void validateParameterCount(Object[] objects, PreparedStatement pstmt) throws SQLException {
        ParameterMetaData parameterMetaData = pstmt.getParameterMetaData();
        if (objects.length != parameterMetaData.getParameterCount()) {
            throw new IllegalArgumentException("파라미터 값의 개수가 올바르지 않습니다");
        }
    }

    private void setParameter(Object[] objects, PreparedStatement pstmt) throws SQLException {
        for (int i = 0; i < objects.length; i++) {
            pstmt.setObject(i + 1, objects[i]);
        }
    }
}
