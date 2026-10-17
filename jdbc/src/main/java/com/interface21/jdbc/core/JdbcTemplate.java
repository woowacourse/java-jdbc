package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
        return execute(sql, pss, PreparedStatement::executeUpdate);
    }

    public <T> Optional<T> queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return queryForObject(sql, rowMapper, createSetter(args));
    }

    public <T> Optional<T> queryForObject(final String sql, final RowMapper<T> rowMapper, final PreparedStatementSetter pss) {
        final List<T> results = query(sql, rowMapper, pss);
        if (results.size() > 1) {
            throw new DataAccessException("결과가 1건이어야 하지만 " + results.size() + "건 조회됨: " + sql);
        }
        return results.stream().findFirst();
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return query(sql, rowMapper, createSetter(args));
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final PreparedStatementSetter pss) {
        return execute(sql, pss, pstmt -> {
            try (ResultSet rs = pstmt.executeQuery()) {
                final List<T> results = new ArrayList<>();
                int rowNum = 0;
                while (rs.next()) {
                    results.add(rowMapper.mapRow(rs, rowNum++));
                }
                return results;
            }
        });
    }

    private <T> T execute(final String sql, final PreparedStatementSetter pss, final PreparedStatementCallback<T> action) {
        log.debug("query : {}", sql);
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pss.setValues(pstmt);
            return action.doInPreparedStatement(pstmt);
        } catch (SQLException e) {
            throw new DataAccessException("SQL 실행 실패: " + sql, e);
        }
    }

    private PreparedStatementSetter createSetter(final Object... args) {
        return pstmt -> {
            if (args == null) {
                return;
            }
            for (int i = 0; i < args.length; i++) {
                pstmt.setObject(i + 1, args[i]);
            }
        };
    }
}
