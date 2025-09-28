package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
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

    public void update(final String sql, final Object... args) {
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement pstmt = createPreparedStatement(conn, sql, args)) {
            pstmt.executeUpdate();
        } catch (final SQLException e) {
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowmapper, Object... args) {
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement pstmt = createPreparedStatement(conn, sql, args);
             final ResultSet rs = pstmt.executeQuery()) {
            List<T> list = new ArrayList<>();
            while (rs.next()) {
                list.add(rowmapper.mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowmapper, Object... args) {
        List<T> results = query(sql, rowmapper, args);
        if (results == null || results.isEmpty()) {
            return null;
        }
        if (results.size() > 1) {
            throw new IllegalStateException("More than one row returned from query");
        }
        return results.get(0);
    }

    private PreparedStatement createPreparedStatement(final Connection conn, final String sql, final Object... args)
            throws SQLException {
        final PreparedStatement pstmt = conn.prepareStatement(sql);
        for (int i = 0; i < args.length; i++) {
            pstmt.setObject(i + 1, args[i]);
        }
        return pstmt;
    }
}
