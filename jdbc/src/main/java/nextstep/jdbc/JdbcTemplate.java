package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

    public void update(final String sql, final Object... args) {
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement pstmt = conn.prepareStatement(sql)) {

            log.debug("query : {}", sql);

            for (int i = 0; i < args.length; i++) {
                pstmt.setObject(i + 1, args[i]);
            }
            pstmt.executeUpdate();
        } catch (final SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException("Failed to execute a sql.", e);
        }
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper) {
        return query(sql, new RowMapperResultSetExecutor<>(rowMapper));
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return query(sql, new RowMapperResultSetExecutor<>(rowMapper), args).get(0);
    }

    private <T> T query(final String sql, final ResultSetExecutor<T> executor, final Object... args) {
        ResultSet rs = null;
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement pstmt = conn.prepareStatement(sql)) {

            log.debug("query : {}", sql);

            for (int i = 0; i < args.length; i++) {
                pstmt.setObject(i + 1, args[i]);
            }

            rs = pstmt.executeQuery();
            return executor.extractData(rs);
        } catch (final SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException("Failed to execute a sql.", e);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (final SQLException ignored) {
            }
        }
    }
}
