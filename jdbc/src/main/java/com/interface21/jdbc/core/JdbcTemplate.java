package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
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

    public int update(String sql, Object... args) {
        return update(sql, createPreparedStatementSetter(args));
    }

    public int update(String sql, PreparedStatementSetter setter) {
        return execute(sql, setter, PreparedStatement::executeUpdate);
    }

    public <T> T queryForObject(
            String sql,
            RowMapper<T> rowMapper,
            Object... args
    ) {
        return execute(sql, createPreparedStatementSetter(args), pstmt -> {
            try (ResultSet rs = pstmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                T result = rowMapper.mapRow(rs);
                if (rs.next()) {
                    throw new DataAccessException("단건 조회 결과는 최대 1건이어야 합니다.");
                }
                return result;
            }
        });
    }

    public <T> List<T> query(
            String sql,
            RowMapper<T> rowMapper,
            Object... args
    ) {
        return execute(sql, createPreparedStatementSetter(args), pstmt -> {
            List<T> result = new ArrayList<>();
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    result.add(rowMapper.mapRow(rs));
                }
                return result;
            }
        });
    }

    private <T> T execute(
            String sql,
            PreparedStatementSetter setter,
            PreparedStatementCallback<T> callback
    ) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setter.setValues(pstmt);
            return callback.doInPreparedStatement(pstmt);
        } catch (SQLException e) {
            throw new DataAccessException("SQL 실행에 실패했습니다.", e);
        }
    }

    private PreparedStatementSetter createPreparedStatementSetter(Object[] args) {
        return pstmt -> {
            for (int i = 0; i < args.length; i++) {
                pstmt.setObject(i + 1, args[i]);
            }
        };
    }

    @FunctionalInterface
    private interface PreparedStatementCallback<T> {
        T doInPreparedStatement(PreparedStatement pstmt) throws SQLException;
    }
}
