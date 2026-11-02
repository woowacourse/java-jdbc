package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(final String sql, final Object... args) {
        return update(sql, createSetter(args));
    }

    public int update(final String sql, final PreparedStatementSetter pss) {
        return execute(sql, pss, pstmt -> pstmt.executeUpdate());
    }

    public <T> Optional<T> queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        final List<T> results = query(sql, rowMapper, args);
        if (results.size() > 1) {
            throw new DataAccessException("[ERROR] 결과가 " + results.size() + "건이 조회되었습니다.");
        }
        if (results.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(results.getFirst());
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return query(sql, rowMapper, createSetter(args));
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final PreparedStatementSetter pss) {
        return execute(sql, pss, pstmt -> {
            try (final ResultSet rs = pstmt.executeQuery()) {
                final List<T> results = new ArrayList<>();
                while (rs.next()) {
                    results.add(rowMapper.mapRow(rs));
                }
                return results;
            }
        });
    }

    private PreparedStatementSetter createSetter(final Object... args) {
        return pstmt -> {
            for (int i = 0; i < args.length; i++) {
                pstmt.setObject(i + 1, args[i]);
            }
        };
    }

    private <T> T execute(final String sql, final PreparedStatementSetter pss,
                          final PreparedStatementCallback<T> action) {
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement pstmt = conn.prepareStatement(sql)) {

            log.debug("query : {}", sql);

            pss.setValues(pstmt);

            return action.doInPreparedStatement(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }
}
