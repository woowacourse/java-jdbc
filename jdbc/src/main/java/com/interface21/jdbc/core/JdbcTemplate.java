package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

public class JdbcTemplate {

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(Connection conn, String sql, Object... args) {
        return update(conn, sql, new ArgumentPreparedStatementSetter(args));
    }

    private int update(Connection conn, String sql, PreparedStatementSetter pss) {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            pss.setValues(ps);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) {
        try (Connection conn = dataSource.getConnection()) {
            return query(conn, sql, rowMapper, new ArgumentPreparedStatementSetter(args));
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private <T> List<T> query(Connection conn, String sql, RowMapper<T> rowMapper, PreparedStatementSetter pss) {
        List<T> results = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            pss.setValues(ps);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(rowMapper.mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
        return results;
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        try (Connection conn = dataSource.getConnection()) {
            return queryForObject(conn, sql, rowMapper, new ArgumentPreparedStatementSetter(args));
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private <T> T queryForObject(Connection conn, String sql, RowMapper<T> rowMapper, PreparedStatementSetter pss) {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            pss.setValues(ps);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rowMapper.mapRow(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }
}
