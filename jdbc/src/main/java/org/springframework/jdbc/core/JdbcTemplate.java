package org.springframework.jdbc.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);
    private static final int SINGLE_SIZE = 1;

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(String sql, Object... args) {
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            log.debug("query : {}", sql);

            setParameters(pstmt, args);

            return pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private void setParameters(PreparedStatement pstmt, Object[] args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            pstmt.setObject(i + 1, args[i]);
        }
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) {
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            log.debug("query : {}", sql);

            setParameters(pstmt, args);

            try (ResultSet resultSet = pstmt.executeQuery()) {
                List<T> results = new ArrayList<>();
                while (resultSet.next()) {
                    results.add(rowMapper.mapRow(resultSet));
                }
                return results;
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @Nullable
    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        final List<T> objects = query(sql, rowMapper, args);
        return getSingleObject(objects);
    }

    private <T> T getSingleObject(List<T> objects) {
        validateEmpty(objects);
        validateSingleSize(objects);
        return objects.iterator().next();
    }

    private <T> void validateEmpty(List<T> objects) {
        if (objects.isEmpty()) {
            throw new IllegalArgumentException("조회 데이터가 존재하지 않습니다.");
        }
    }

    private <T> void validateSingleSize(List<T> objects) {
        if (objects.size() > SINGLE_SIZE) {
            throw new IllegalArgumentException("조회 데이터가 한 개 이상 존재합니다.");
        }
    }
}
