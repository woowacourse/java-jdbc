package com.interface21.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(final String sql, final Object... params) {
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            setParams(ps, params);
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private void setParams(PreparedStatement ps, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            ps.setObject(i + 1, params[i]);
        }
    }

    public <T> T query(final String sql, final RowMapper<T> rowMapper, final Object... params) {
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            setParams(ps, params);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rowMapper.mapRow(rs);
            }
            return null;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public <T> List<T> queryForList(final String sql, final RowMapper<T> rowMapper, final Object... params) {
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            setParams(ps, params);
            ResultSet rs = ps.executeQuery();
            List<T> list = new ArrayList<>();
            while (rs.next()) {
                list.add(rowMapper.mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
