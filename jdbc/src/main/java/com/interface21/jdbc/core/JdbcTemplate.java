package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
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

    public int update(String sql, Object... args) {
        Connection conn = DataSourceUtils.getConnection(dataSource);
        PreparedStatementSetter pss = new ArgumentPreparedStatementSetter(args);
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            pss.setValues(ps);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) {
        List<T> results = new ArrayList<>();
        Connection conn = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            PreparedStatementSetter pss = new ArgumentPreparedStatementSetter(args);
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
        Connection conn = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            PreparedStatementSetter pss = new ArgumentPreparedStatementSetter(args);
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
