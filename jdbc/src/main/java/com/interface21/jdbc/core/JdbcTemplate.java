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

    // TODO 한개가 아닐 경우 예외 처리
    public <T> T executeQueryObject(
        final String sql,
        final RowMapper<T> rowMapper,
        final Object... args
    ) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = getPreparedStatementWithArguments(conn, sql, args);
             ResultSet resultSet = pstmt.executeQuery();
        ) {
            log.debug("query : {}", sql);
            return rowMapper.mapRow(resultSet);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public <T> List<T> executeQuery(final String sql, final RowMapper<T> rowMapper, final Object... args) {
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

    public int executeUpdate(String sql, Object... args) {
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
        for (int i = 1; i <= args.length; i++) {
            pstmt.setObject(i, args[i]);
        }
        return pstmt;
    }
}
