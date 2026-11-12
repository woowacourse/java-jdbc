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

    public void update(final String sql, Object... args) {
        execute(sql, updateCallback(args));
    }

    public void update(final Connection connection, final String sql, Object... args) {
        execute(connection, sql, updateCallback(args));
    }

    private <T> T execute(String sql, PreparedStatementCallback<T> action) {
        try (Connection connection = dataSource.getConnection()) {
            return execute(connection, sql, action);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private <T> T execute(Connection connection, String sql, PreparedStatementCallback<T> action) {
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            T result = action.doInPreparedStatement(pstmt);

            log.debug("query : {}", sql);
            return result;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    public <T> List<T> query(final String sql, RowMapper<T> rowMapper, Object... args) {
        return execute(sql, queryCallback(rowMapper, args));
    }

    public <T> List<T> query(final Connection connection, final String sql, RowMapper<T> rowMapper, Object... args) {
        return execute(connection, sql, queryCallback(rowMapper, args));
    }

    public <T> T queryForObject(final String sql, RowMapper<T> rowMapper, Object... args) {
        return singleResult(query(sql, rowMapper, args));
    }

    public <T> T queryForObject(final Connection connection, final String sql, RowMapper<T> rowMapper, Object... args) {
        return singleResult(query(connection, sql, rowMapper, args));
    }

    private PreparedStatementCallback<Integer> updateCallback(Object... args) {
        return pstmt -> {
            setObject(pstmt, args);
            return pstmt.executeUpdate();
        };
    }

    private <T> PreparedStatementCallback<List<T>> queryCallback(RowMapper<T> rowMapper, Object... args) {
        return pstmt -> {
            if (args != null) {
                setObject(pstmt, args);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                List<T> results = new ArrayList<>();

                while (rs.next()) {
                    results.add(rowMapper.mapRow(rs));
                }

                return results;
            }
        };
    }

    private <T> T singleResult(List<T> results) {
        if (results.isEmpty()) {
            throw new IllegalStateException("조회 결과를 찾을 수 없습니다.");
        }
        if (results.size() > 1) {
            throw new IllegalStateException("조회 결과가 2개 이상입니다.");
        }

        return results.getFirst();
    }

    private void setObject(PreparedStatement pstmt, Object... args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            pstmt.setObject(i + 1, args[i]);
        }
    }
}
