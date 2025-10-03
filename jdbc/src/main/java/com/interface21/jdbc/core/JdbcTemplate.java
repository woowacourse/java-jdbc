package com.interface21.jdbc.core;

import java.sql.Connection;
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

    public <T> T queryForObject(
        final String sql,
        final RowMapper<T> rowMapper,
        final Object... args
    ) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = getPreparedStatementWithArguments(conn, sql, args);
             ResultSet resultSet = pstmt.executeQuery();
        ) {
            log.debug("query : {}", sql);

            if (!resultSet.next()) { // 쿼리 결과 없음
                return null;
            }
            T returnValue = rowMapper.mapRow(resultSet);
            if (resultSet.next()) { // 결과 1개 초과
                throw new IllegalArgumentException("query returns more than one row");
            }
            return returnValue;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = getPreparedStatementWithArguments(conn, sql, args);
             ResultSet resultSet = pstmt.executeQuery();
        ) {
            log.debug("query : {}", sql);
            List<T> list = new ArrayList<>();
            while (resultSet.next()) {
                list.add(rowMapper.mapRow(resultSet));
            }
            return list;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public int update(String sql, Object... args) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = getPreparedStatementWithArguments(conn, sql, args);
        ) {
            log.debug("query : {}", sql);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private PreparedStatement getPreparedStatementWithArguments(
        final Connection conn,
        final String sql,
        final Object... args
    ) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement(sql);
        for (int i =0; i < args.length; i++) {
            pstmt.setObject(i + 1, args[i]);
        }
        return pstmt;
    }
}
