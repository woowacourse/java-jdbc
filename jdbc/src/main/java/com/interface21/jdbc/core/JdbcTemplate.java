package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
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

    public <T> List<T> queryForResultList(String sql, RowMapper<T> rowMapper, Object... args) {
        return query(sql, rs -> {
            List<T> results = new ArrayList<>();
            while (rs.next()) {
                results.add(rowMapper.mapRow(rs));
            }
            return results;
        }, args);
    }

    public <T> Optional<T> queryForResult(String sql, RowMapper<T> rowMapper, Object... args) {
        return query(sql, rs -> {
            if (rs.next()) {
                return Optional.of(rowMapper.mapRow(rs));
            }
            return Optional.empty();
        }, args);
    }

    public void queryForUpdate(final String sql, final Object... args) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            setParameter(args, pstmt);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private <T> T query(String sql, ResultProcessor<T> extractor, Object... args) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            setParameter(args, pstmt);
            try (ResultSet rs = pstmt.executeQuery()) {
                return extractor.processResult(rs);
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private void setParameter(Object[] args, PreparedStatement pstmt) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            pstmt.setObject(i + 1, args[i]);
        }
    }
}
