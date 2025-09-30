package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.EmptyResultDataAccessException;
import com.interface21.jdbc.IncorrectResultSizeDataAccessException;
import com.interface21.jdbc.rowmapper.RowMapper;
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

    public void update(String sql, Object... parameters) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.debug("update : {}", sql);

            for (int i = 1; i <= parameters.length; i++) {
                pstmt.setObject(i, parameters[i - 1]);
            }
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... parameters) {
        List<T> results = query(sql, rowMapper, parameters);

        if (results.isEmpty()) {
            throw new EmptyResultDataAccessException("Query returned no results.");
        }

        return results.getFirst();
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... parameters) {
        final var results = new ArrayList<T>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int i = 1; i <= parameters.length; i++) {
                pstmt.setObject(i, parameters[i - 1]);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                log.debug("query : {}", sql);
                int rowNum = 0;
                while (rs.next()) {
                    rowNum++;
                    results.add(rowMapper.rowMap(rs, rowNum));
                }
                return results;
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }
}
