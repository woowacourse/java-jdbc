package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(final String sql, final PreparedStatementSetter pss) {
        try (final var conn = dataSource.getConnection();
             final var pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);

            pss.setValues(pstmt);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final PreparedStatementSetter pss) {
        try (final var conn = dataSource.getConnection();
             final var pstmt = conn.prepareStatement(sql);
             final var rs = executeQuery(pss, pstmt)) {
            log.debug("query : {}", sql);

            final var results = new ArrayList<T>();
            while (rs.next()) {
                results.add(rowMapper.mapRow(rs));
            }
            return results;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final PreparedStatementSetter pss) {
        final var results = query(sql, rowMapper, pss);
        if (results.isEmpty()) {
            return null;
        }
        if (results.size() > 1) {
            throw new DataAccessException("조회 결과가 1개보다 많습니다. size: " + results.size());
        }
        return results.getFirst();
    }

    private ResultSet executeQuery(final PreparedStatementSetter pss, final PreparedStatement pstmt) throws SQLException {
        pss.setValues(pstmt);
        return pstmt.executeQuery();
    }
}
