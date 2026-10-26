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

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(final String sql, final Object... args) {
        return execute(sql, pstmt -> {
            for (int i = 0; i < args.length; i++) {
                pstmt.setObject(i + 1, args[i]);
            }

            return pstmt.executeUpdate();
        });
    }

    public <R> R queryForObject(final String sql, final RowMapper<R> rowMapper, Object... args) {
        return execute(sql, pstmt -> {
            for (int k = 0; k < args.length; k++) {
                pstmt.setObject(k + 1, args[k]);
            }

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rowMapper.mapRow(rs, 0);
            }
            return null;
        });
    }

    public <R> List<R> query(final String sql, final RowMapper<R> rowMapper, Object... args) {
        return execute(sql, pstmt -> {
            for (int k = 0; k < args.length; k++) {
                pstmt.setObject(k + 1, args[k]);
            }

            ResultSet rs = pstmt.executeQuery();

            List<R> list = new ArrayList<>();

            int rowNum = 0;

            while (rs.next()) {
                list.add(rowMapper.mapRow(rs, rowNum));
                rowNum++;
            }

            return list;
        });
    }

    private <T> T execute(
            final String sql,
            final PreparedStatementCallback<T> callback
    ) {
        log.debug("query : {}", sql);

        try (
            Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            return callback.execute(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    @FunctionalInterface
    private interface PreparedStatementCallback<T> {
        T execute(PreparedStatement pstmt) throws SQLException;
    }
}
