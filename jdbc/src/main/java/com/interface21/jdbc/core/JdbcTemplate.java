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
    private static final String TOO_MANY_RESULTS_MESSAGE = "단건 조회 결과가 1개를 초과했습니다. size=%d";

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(final String sql, final Object... args) {
        update(sql, argumentsSetter(args));
    }

    public void update(final String sql, final PreparedStatementSetter pss) {
        execute(sql, pss, PreparedStatement::executeUpdate);
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return query(sql, rowMapper, argumentsSetter(args));
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final PreparedStatementSetter pss) {
        return execute(sql, pss, pstmt -> mapRows(pstmt, rowMapper));
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper,
            final Object... args) {
        return queryForObject(sql, rowMapper, argumentsSetter(args));
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper,
            final PreparedStatementSetter pss) {
        final List<T> results = query(sql, rowMapper, pss);
        if (results.isEmpty()) {
            return null;
        }
        if (results.size() > 1) {
            throw new DataAccessException(TOO_MANY_RESULTS_MESSAGE.formatted(results.size()));
        }
        return results.get(0);
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

    private <T> List<T> mapRows(final PreparedStatement pstmt, final RowMapper<T> rowMapper) throws SQLException {
        try (final ResultSet rs = pstmt.executeQuery()) {
            final List<T> result = new ArrayList<>();
            while (rs.next()) {
                result.add(rowMapper.mapRow(rs));
            }
            return result;
        }
    }

    private PreparedStatementSetter argumentsSetter(final Object... args) {
        return pstmt -> {
            for (int i = 0; i < args.length; i++) {
                pstmt.setObject(i + 1, args[i]);
            }
        };
    }
}
