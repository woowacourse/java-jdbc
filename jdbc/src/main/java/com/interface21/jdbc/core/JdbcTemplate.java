package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.CannotGetJdbcConnectionException;
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

    public void update(String sql, Object... parameters) {
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            bindPreparedStatement(pstmt, parameters);
            executeUpdateQuery(pstmt);
        } catch (SQLException e) {
            log.error("SQL 예외 발생: {}", e.getMessage(), e);
            throw new DataAccessException("SQL 예외 발생", e);
        }
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... parameters) {
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            bindPreparedStatement(pstmt, parameters);
            ResultSet resultSet = executeQuery(pstmt);
            return bindQueryResult(resultSet, rowMapper);
        } catch (SQLException e) {
            log.error("SQL 예외 발생: {}", e.getMessage(), e);
            throw new DataAccessException("SQL 예외 발생", e);
        }
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... parameters) {
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            bindPreparedStatement(pstmt, parameters);
            ResultSet resultSet = executeQuery(pstmt);
            return bindQueryResults(resultSet, rowMapper);
        } catch (SQLException e) {
            log.error("SQL 예외 발생: {}", e.getMessage(), e);
            throw new DataAccessException("SQL 예외 발생", e);
        }
    }


    private Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            log.error("DB 연결 실패: {}", e.getMessage(), e);
            throw new CannotGetJdbcConnectionException("DB 연결 실패", e);
        }
    }

    private PreparedStatement bindPreparedStatement(PreparedStatement pstmt, Object[] params) {
        try {
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
            return pstmt;
        } catch (SQLException e) {
            log.error("쿼리의 파라미터 바인딩 실패: {}", e.getMessage(), e);
            throw new DataAccessException("쿼리의 파라미터 바인딩 실패", e);
        }
    }

    private void executeUpdateQuery(PreparedStatement pstmt) {
        try {
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error("데이터 추가/수정 실패: {}", e.getMessage(), e);
            throw new DataAccessException("데이터 추가/수정 실패", e);
        }
    }

    private ResultSet executeQuery(PreparedStatement pstmt) {
        try {
            return pstmt.executeQuery();
        } catch (SQLException e) {
            log.error("데이터 조회 실패: {}", e.getMessage(), e);
            throw new DataAccessException("데이터 조회 실패", e);
        }
    }

    private <T> List<T> bindQueryResults(ResultSet resultSet, RowMapper<T> rowMapper) {
        try {
            List<T> results = new ArrayList<>();
            int rowNum = 0;
            while (resultSet.next()) {
                T result = rowMapper.mapRowToObject(resultSet, rowNum++);
                results.add(result);
            }
            return results;
        } catch (SQLException e) {
            log.error("조회 결과 바인딩 실패: {}", e.getMessage(), e);
            throw new DataAccessException("조회 결과 바인딩 실패", e);
        }
    }

    private <T> T bindQueryResult(ResultSet resultSet, RowMapper<T> rowMapper) {
        try {
            resultSet.next();
            return rowMapper.mapRowToObject(resultSet, 0);
        } catch (SQLException e) {
            log.error("조회 결과 바인딩 실패: {}", e.getMessage(), e);
            throw new DataAccessException("조회 결과 바인딩 실패", e);
        }
    }
}
