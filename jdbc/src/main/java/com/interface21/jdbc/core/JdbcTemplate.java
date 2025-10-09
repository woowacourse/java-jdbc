package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class JdbcTemplate {

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(final String sql, final Object... params) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            setParameters(pstmt, params);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new SqlExecutionException("SQL 실행 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    public <T> List<T> query(final String sql, final Function<ResultSet, T> mapper, final Object... params) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = executeQuery(pstmt, params)) {
            return extractResults(rs, mapper);
        } catch (SQLException e) {
            throw new SqlExecutionException("SQL 실행 중 오류가 발생했습니다: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RowMappingException("ResultSet을 객체로 매핑하는 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    private ResultSet executeQuery(PreparedStatement pstmt, Object... params) throws SQLException {
        setParameters(pstmt, params);
        return pstmt.executeQuery();
    }

    private <T> List<T> extractResults(ResultSet rs, Function<ResultSet, T> mapper) throws SQLException {
        List<T> results = new ArrayList<>();
        while (rs.next()) {
            results.add(mapper.apply(rs));
        }
        return results;
    }

    public <T> T queryForObject(final String sql, final Function<ResultSet, T> mapper, final Object... params) {
        List<T> results = query(sql, mapper, params);

        if (results.isEmpty()) {
            throw new InvalidResultException("조회 결과가 없습니다");
        }
        if (results.size() > 1) {
            throw new InvalidResultException("조회 결과가 2개 이상입니다");
        }

        return results.getFirst();
    }

    private void setParameters(PreparedStatement pstmt, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            pstmt.setObject(i + 1, params[i]);
        }
    }
}
