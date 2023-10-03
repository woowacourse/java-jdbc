package org.springframework.jdbc.core;

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
    private final PreparedStatementExecutor preparedStatementExecutor;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
        preparedStatementExecutor = new PreparedStatementExecutor(dataSource);
    }

    public void update(final String sql, final Object... values) {
        preparedStatementExecutor.execute(
                conn -> {
                    final PreparedStatement psmt = conn.prepareStatement(sql);
                    setValues(psmt, values);
                    return psmt;
                },
                PreparedStatement::executeUpdate
        );
    }

    public <T> T queryForObject(final String sql, final Mapper<T> mapper, final Object... values) {
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement psmt = conn.prepareStatement(sql)) {
            setValues(psmt, values);
            final ResultSet rs = psmt.executeQuery();
            log.info("query : {}", sql);
            if (rs.next()) {
                return mapper.map(rs);
            }
            return null;
        } catch (final SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public <T> List<T> query(final String sql, final Mapper<T> rowMapper, final Object... values) {
        return queryForObject(sql, rs -> {
            final List<T> results = new ArrayList<>();
            do {
                results.add(rowMapper.map(rs));
            } while (rs.next());
            return results;
        }, values);
    }

    private void setValues(final PreparedStatement psmt, final Object[] values) throws SQLException {
        for (int i = 0; i < values.length; i++) {
            psmt.setObject(i + 1, values[i]);
        }
    }
}
