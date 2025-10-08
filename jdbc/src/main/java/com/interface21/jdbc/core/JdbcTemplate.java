package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import com.interface21.dao.EmptyResultDataAccessException;
import com.interface21.dao.IncorrectResultSizeDataAccessException;
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

    private <T> T execute(final PreparedStatementCreator psc, final PreparedStatementCallback<T> action) {
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement ps = psc.createPreparedStatement(conn)) {
            return action.doInPreparedStatement(ps);
        } catch (final SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public int update(final String sql, final PreparedStatementSetter pss) {
        return execute(conn -> conn.prepareStatement(sql), ps -> {
            pss.setValues(ps);
            return ps.executeUpdate();
        });
    }

    public int update(final String sql, final Object... args) {
        return update(sql, createPreparedStatementSetter(args));
    }

    public <T> List<T> query(final String sql, final PreparedStatementSetter pss, final RowMapper<T> rowMapper) {
        return execute(conn -> conn.prepareStatement(sql), ps -> {
            pss.setValues(ps);
            try (final ResultSet rs = ps.executeQuery()) {
                final List<T> results = new ArrayList<>();
                while (rs.next()) {
                    results.add(rowMapper.mapRow(rs));
                }
                return results;
            }
        });
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return query(sql, createPreparedStatementSetter(args), rowMapper);
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        final List<T> results = query(sql, rowMapper, args);
        return getSingleResult(results);
    }

    public <T> T queryForObject(final String sql, final PreparedStatementSetter pss, final RowMapper<T> rowMapper) {
        final List<T> results = query(sql, pss, rowMapper);
        return getSingleResult(results);
    }

    private PreparedStatementSetter createPreparedStatementSetter(final Object[] args) {
        return ps -> {
            for (int i = 0; i < args.length; i++) {
                ps.setObject(i + 1, args[i]);
            }
        };
    }

    private <T> T getSingleResult(final List<T> results) {
        if (results.isEmpty()) {
            throw new EmptyResultDataAccessException(1);
        }
        if (results.size() > 1) {
            throw new IncorrectResultSizeDataAccessException(1, results.size());
        }
        return results.get(0);
    }
}
