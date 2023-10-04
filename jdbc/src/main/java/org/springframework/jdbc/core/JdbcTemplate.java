package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplateException.MoreDataAccessException;
import org.springframework.jdbc.core.JdbcTemplateException.NoDataAccessException;
import org.springframework.jdbc.core.JdbcTemplateException.DatabaseAccessException;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void execute(final String sql, final Object... fields) {
        log.debug("query : {}", sql);

        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement ps = conn.prepareStatement(sql)) {

            setPreparedStatement(ps, fields);
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DatabaseAccessException(e.getMessage());
        }
    }

    private void setPreparedStatement(final PreparedStatement ps, final Object[] fields) throws SQLException {
        for (int i = 1; i <= fields.length; i++) {
            ps.setObject(i, fields[i-1]);
        }
    }

    public <T> T find(final String sql,
                      final RowMapper<T> rowMapper,
                      final Object... fields) {
        log.debug("query : {}", sql);

        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement ps = conn.prepareStatement(sql)) {

            setPreparedStatement(ps, fields);
            return getObject(rowMapper, ps);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DatabaseAccessException(e.getMessage());
        }
    }

    private <T> T getObject(final RowMapper<T> rowMapper, final PreparedStatement ps) throws SQLException {
        try (final ResultSet rs = ps.executeQuery()) {
            return executeRowMapper(rowMapper, rs);
        }
    }

    private <T> T executeRowMapper(final RowMapper<T> rowMapper, final ResultSet rs) throws SQLException {
        if (rs.next() && rs.isLast()) {
            return rowMapper.mapRow(rs);
        }
        if (!rs.next()) {
            throw new NoDataAccessException();
        }
        throw new MoreDataAccessException();
    }

    public <T> List<T> findAll(final String sql, final RowMapper<T> rowMapper) {
        log.debug("query : {}", sql);

        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement ps = conn.prepareStatement(sql);
             final ResultSet rs = ps.executeQuery()) {

            return getObjects(rowMapper, rs);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DatabaseAccessException(e.getMessage());
        }
    }

    private <T> List<T> getObjects(final RowMapper<T> rowMapper, final ResultSet rs) throws SQLException {
        final List<T> objects = new ArrayList<>();
        while (rs.next()) {
            objects.add(rowMapper.mapRow(rs));
        }
        return objects;
    }
}
