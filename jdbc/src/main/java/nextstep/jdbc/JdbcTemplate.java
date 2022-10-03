package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(final String sql, final Object... parameters) {
        final SqlExecutor<Integer> executor = PreparedStatement::executeUpdate;
        execute(sql, executor, parameters);
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... parameters) {
        final SqlExecutor<T> executor = pstmt -> {
            try (final ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rowMapper.mapRow(rs);
                }
            }
            return null;
        };
        return execute(sql, executor, parameters);
    }

    private <T> T execute(final String sql, final SqlExecutor<T> executor, final Object... parameters) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = dataSource.getConnection();
            pstmt = conn.prepareStatement(sql);

            log.debug("query : {}", sql);

            for (int i = 0; i < parameters.length; i++) {
                pstmt.setObject(i + 1, parameters[i]);
            }

            return executor.execute(pstmt);

        } catch (final SQLException e) {
            log.error(e.getMessage(), e);
            throw new JdbcConnectionException("Fail to get JDBC Connection", e);
        } finally {
            try {
                if (pstmt != null) {
                    pstmt.close();
                }
            } catch (final SQLException ignored) {
            }

            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (final SQLException ignored) {
            }
        }
    }
}
