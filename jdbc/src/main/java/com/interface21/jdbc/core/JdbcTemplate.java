package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
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

    public void update(final String sql, final PreparedStatementSetter pss) {
        execute(sql, pss, pstmt -> {
            pstmt.executeUpdate();
            return null;
        });
    }

    public void update(final String sql, final Object... parameters) {
        update(sql, createPreparedStatementSetter(parameters));
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rm, final PreparedStatementSetter pss) {
        return execute(sql, pss, pstmt -> mapResultSet(rm, pstmt));
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rm, final Object... parameters) {
        return query(sql, rm, createPreparedStatementSetter(parameters));
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rm, final Object... parameters) {
        final var list = query(sql, rm, parameters);
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    private <T> List<T> mapResultSet(final RowMapper<T> rm, final PreparedStatement pstmt) {
        try (final ResultSet rs = pstmt.executeQuery()) {
            final List<T> list = new ArrayList<>();
            while (rs.next()) {
                list.add(rm.mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private <T> T execute(final String sql, final PreparedStatementSetter pss,
                          final PreparedStatementCallback<T> action) {
        try (final var conn = dataSource.getConnection(); final var pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            pss.setParameters(pstmt);
            return action.doInPreparedStatement(pstmt);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private PreparedStatementSetter createPreparedStatementSetter(final Object... parameters) {
        return pstmt -> {
            for (int i = 0; i < parameters.length; i++) {
                pstmt.setObject(i + 1, parameters[i]);
            }
        };
    }
}
