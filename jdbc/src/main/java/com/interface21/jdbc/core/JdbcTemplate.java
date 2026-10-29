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

    public int update(final String sql, final Object... parameters) {
        return execute(PreparedStatement::executeUpdate, sql, parameters);
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... parameters) {
        return execute(pstmt -> {
            try (final ResultSet rs = pstmt.executeQuery()) {
                final List<T> results = new ArrayList<>();
                while (rs.next()) {
                    results.add(rowMapper.mapRow(rs));
                }
                return results;
            }
        }, sql, parameters);
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... parameters) {
        final List<T> results = query(sql, rowMapper, parameters);
        if (results.isEmpty()) {
            return null;
        }
        if (results.size() > 1) {
            throw new DataAccessException("결과가 1개보다 많습니다. size=" + results.size());
        }
        return results.getFirst();
    }

    private PreparedStatementSetter createPreparedStatementSetter(final Object... parameters) {
        return pstmt -> {
            for (int i = 0; i < parameters.length; i++) {
                pstmt.setObject(i + 1, parameters[i]);
            }
        };
    }

    private <T> T execute(final PreparedStatementCallback<T> pstmtcb, final String sql, final Object... parameters) {
        try (
                final var conn = dataSource.getConnection();
                final var pstmt = conn.prepareStatement(sql)
        ) {
            createPreparedStatementSetter(parameters).setParameters(pstmt);
            return pstmtcb.execute(pstmt);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }
}
