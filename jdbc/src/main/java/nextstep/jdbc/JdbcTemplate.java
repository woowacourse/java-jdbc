package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DataSourceUtils;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(final String sql, final Object... params) {
        execute(sql, pstmt -> {
            setParameters(pstmt, params);
            return pstmt.executeUpdate();
        });
    }

    public <T> Optional<T> queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... params) {
        return execute(sql, pstmt -> {
            setParameters(pstmt, params);
            final ResultSet rs = pstmt.executeQuery();
            return ResultSetExtractor.extractForObject(rowMapper, rs);
        });
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... params) {
        return execute(sql, pstmt -> {
            setParameters(pstmt, params);
            final ResultSet rs = pstmt.executeQuery();
            return ResultSetExtractor.extract(rowMapper, rs);
        });
    }

    private <T> T execute(final String sql, final PreparedStatementExecutor<T> preparedStatementExecutor) {
        final Connection conn = DataSourceUtils.getConnection(dataSource);
        try (final PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            return preparedStatementExecutor.execute(pstmt);
        } catch (final SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    private void setParameters(final PreparedStatement pstmt, final Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            pstmt.setObject(i + 1, params[i]);
        }
    }
}
