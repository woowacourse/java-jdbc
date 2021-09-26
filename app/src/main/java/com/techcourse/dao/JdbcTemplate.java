package com.techcourse.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);
    public static final String SQL_INFO_LOG = "query : {}";

    private final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private ResultSet executeQuery(PreparedStatement pstmt) throws SQLException {
        return pstmt.executeQuery();
    }

    public void update(String sql, PreparedStatementSetter setter) {
        try (Connection conn = dataSource.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setter.setValues(pstmt);
            if (log.isDebugEnabled()) {
                log.debug(SQL_INFO_LOG, sql);
            }
            pstmt.executeUpdate();
        } catch (SQLException exception) {
            throw new DataAccessException(exception);
        }
    }

    public <T> T query(String sql, RowMapper<T> rowMapper, PreparedStatementSetter setter) {
        try (Connection conn = dataSource.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setter.setValues(pstmt);
            if (log.isDebugEnabled()) {
                log.debug(SQL_INFO_LOG, sql);
            }
            ResultSet rs = executeQuery(pstmt);

            if (!rs.next()) {
                return null;
            }
            return rowMapper.mapRow(rs);
        } catch (SQLException exception) {
            throw new DataAccessException(exception);
        }
    }

    public <T> List<T> queryForList(String sql, RowMapper<T> rowMapper) {
        try (Connection conn = dataSource.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (log.isDebugEnabled()) {
                log.debug(SQL_INFO_LOG, sql);
            }
            ResultSet rs = executeQuery(pstmt);

            List<T> result = new ArrayList<>();
            while (rs.next()) {
                result.add(rowMapper.mapRow(rs));
            }
            return result;
        } catch (SQLException exception) {
            throw new DataAccessException(exception);
        }
    }
}
