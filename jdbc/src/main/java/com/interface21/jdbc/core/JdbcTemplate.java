package com.interface21.jdbc.core;

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

    public void update(String sql, Object... args) {
        execute(sql, null, args, (rm, ps) -> ps.executeUpdate());
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) {
        ResultSetClosePreparedStatementCallBack<T, List<T>> callBack = new ResultSetClosePreparedStatementCallBack<>() {
            @Override
            List<T> createResult(ResultSet rs, RowMapper<T> rm) throws SQLException {
                List<T> entityList = new ArrayList<>();
                while (rs.next()) {
                    T entity = rm.mapRow(rs, rs.getRow());
                    entityList.add(entity);
                }
                return entityList;
            }
        };

        return execute(sql, rowMapper, args, callBack);
    }

    public <T> Optional<T> queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        ResultSetClosePreparedStatementCallBack<T, Optional<T>> callBack = new ResultSetClosePreparedStatementCallBack<>() {
            @Override
            Optional<T> createResult(ResultSet rs, RowMapper<T> rm) throws SQLException {
                if (rs.next()) {
                    T entity = rm.mapRow(rs, rs.getRow());
                    if (rs.next()) {
                        throw new IllegalArgumentException("조회된 레코드가 2건 이상입니다.");
                    }
                    return Optional.ofNullable(entity);
                }
                return Optional.empty();
            }
        };

        return execute(sql, rowMapper, args, callBack);
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    private <T, R> R execute(
            String sql, RowMapper<T> rowMapper, Object[] args, PreparedStatementCallBack<T, R> callBack
    ) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);

            for (int i = 0; i < args.length; i++) {
                ps.setObject(i + 1, args[i]);
            }
            return callBack.call(rowMapper, ps);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private abstract static class ResultSetClosePreparedStatementCallBack<T, R> implements PreparedStatementCallBack<T, R> {
        @Override
        public R call(RowMapper<T> rowMapper, PreparedStatement ps) throws SQLException {
            try (ResultSet rs = ps.executeQuery()) {
                return createResult(rs, rowMapper);
            }
        }

        abstract R createResult(ResultSet resultSet, RowMapper<T> rowMapper) throws SQLException;
    }
}
