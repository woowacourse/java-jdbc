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

    public Long updateAndReturnKey(final String sql, final Object... params) {
        return execute(sql, pstmt -> {
            pstmt = pstmt.getConnection().prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            bindParameters(params, pstmt);
            pstmt.executeUpdate();

            try (final ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong("id");
                }
                throw new DataAccessException("Failed to find return generated key, " + sql);
            }
        });
    }

    public int update(final String sql, final Object... params) {
        return execute(sql, pstmt -> {
            bindParameters(params, pstmt);
            return pstmt.executeUpdate();
        });
    }

    public int update(final Connection conn, final String sql, final Object... params) {
        return execute(conn, sql, pstmt -> {
            bindParameters(params, pstmt);
            return pstmt.executeUpdate();
        });
    }

    public <T> Optional<T> queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... params) {
        return execute(sql, pstmt -> {
            bindParameters(params, pstmt);

            try (final ResultSet rs = pstmt.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }

                final T mappedRow = rowMapper.mapRow(rs);

                if (rs.next()) {
                    throw new DataAccessException("queryForObject 실행 시 조회 결과가 1건 이상입니다.");
                }

                return Optional.ofNullable(mappedRow);
            }
        });
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper) {
        return execute(sql, pstmt ->  {
            try (final ResultSet rs = pstmt.executeQuery()) {
                final List<T> results = new ArrayList<>();
                while (rs.next()) {
                    results.add(rowMapper.mapRow(rs));
                }
                return results;
            }
        });
    }

    private void bindParameters(final Object[] params, final PreparedStatement pstmt) throws SQLException {
        final PreparedStatementSetter pstmts = new ArgumentPreparedStatementSetter(params);
        pstmts.setParameters(pstmt);
    }

    private <T> T execute(final String sql, final PreparedStatementCallBack<T> action) {
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement pstmt = conn.prepareStatement(sql)) {

            log.debug("query : {}", sql);
            return action.doInPreparedStatement(pstmt);

        } catch (final SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private <T> T execute(final Connection conn, final String sql, final PreparedStatementCallBack<T> action) {
        try (final PreparedStatement pstmt = conn.prepareStatement(sql)) {

            log.debug("query : {}", sql);
            return action.doInPreparedStatement(pstmt);

        } catch (final SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }
}
