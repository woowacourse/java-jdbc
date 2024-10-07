package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public final void update(String sql, Object... params) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);

            setParams(pstmt, params);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    public final <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... params) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);

            setParams(pstmt, params);
            try (ResultSet rs = pstmt.executeQuery()) {
                return mapResults(rowMapper, rs);
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    public final <T> Optional<T> queryForObject(String sql, RowMapper<T> rowMapper, Object... params) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);

            setParams(pstmt, params);
            try (ResultSet rs = pstmt.executeQuery()) {
                return mapObjectResult(rowMapper, rs);
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private <T> List<T> mapResults(RowMapper<T> rowMapper, ResultSet rs) throws SQLException {
        List<T> values = new ArrayList<>();
        while (rs.next()) {
            values.add(rowMapper.mapRow(rs));
        }
        return values;
    }

    private <T> Optional<T> mapObjectResult(RowMapper<T> rowMapper, ResultSet rs) throws SQLException {
        if (rs.next()) {
            return Optional.of(rowMapper.mapRow(rs));
        }
        return Optional.empty();
    }

    private void setParams(PreparedStatement pstmt, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            pstmt.setObject(i + 1, params[i]);
        }
    }
}
