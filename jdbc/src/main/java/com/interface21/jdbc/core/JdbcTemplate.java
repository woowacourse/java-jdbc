package com.interface21.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
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

    public void update(String sql, Object...args) {
        final int countValue = countPlaceholders(sql);
        validateArgsCount(countValue, args);
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement preparedStatement = prepareStatement(conn, sql, args);
        ) {
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) {
        final int countValue = countPlaceholders(sql);
        validateArgsCount(countValue, args);
        try (Connection conn = dataSource.getConnection();
                PreparedStatement preparedStatement = prepareStatement(conn, sql, args);
                ResultSet rs = preparedStatement.executeQuery();
        ) {
            List<T> results = new ArrayList<>();
            while (rs.next()) {
                results.add(rowMapper.mapRow(rs));
            }
            return results;

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public <T> Optional<T> queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        final int countValue = countPlaceholders(sql);
        validateArgsCount(countValue, args);
        try (Connection conn = dataSource.getConnection();
                PreparedStatement preparedStatement = prepareStatement(conn, sql, args);
                ResultSet rs = preparedStatement.executeQuery();
        ) {
            if (rs.next()) {
                T result = rowMapper.mapRow(rs);
                if (rs.next()) {
                    throw new RuntimeException("결과가 2개 이상입니다.");
                }
                return Optional.of(result);
            }
            return Optional.empty();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private PreparedStatement prepareStatement(Connection conn, String sql, Object... args)
            throws SQLException {
        final int countValue = countPlaceholders(sql);
        validateArgsCount(countValue, args);

        final PreparedStatement prepareStatement = conn.prepareStatement(sql);
        for (int i = 0; i < args.length; i++) {
            prepareStatement.setObject(i + 1, args[i]);
        }
        return prepareStatement;
    }

    private void validateArgsCount(int count, Object...args) {
        if (args.length != count) {
            throw new IllegalArgumentException("SQL의 파라미터 개수와 전달된 인자 개수가 일치하지 않습니다.");
        }
    }

    private int countPlaceholders(String sql) {
        int count = 0;
        boolean inQuotes = false;

        for (int i = 0; i < sql.length(); i++) {
            char c = sql.charAt(i);

            // 작은따옴표 처리 (문자열 리터럴 내부의 ?는 제외)
            if (c == '\'') {
                inQuotes = !inQuotes;
            }

            // 작은따옴표 밖에 있는 ?만 카운트
            if (c == '?' && !inQuotes) {
                count++;
            }
        }

        return count;
    }

}
