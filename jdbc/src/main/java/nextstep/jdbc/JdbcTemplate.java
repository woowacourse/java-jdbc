package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import nextstep.jdbc.support.DataAccessUtils;
import nextstep.jdbc.support.StatementUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(final String sql, final Object... args) {
        return executeQuery(sql, PreparedStatement::executeUpdate, args);
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return executeQuery(sql, pstmt -> executeAndReturnResults(pstmt, rowMapper), args);
    }

    private static <T> List<T> executeAndReturnResults(final PreparedStatement pstmt, final RowMapper<T> rowMapper)
            throws SQLException {
        try (ResultSet rs = pstmt.executeQuery()) {
            return DataAccessUtils.getResults(rowMapper, rs);
        }
    }

    private <T> T executeQuery(final String sql, final QueryExecutor<T> queryExecutor, final Object... args) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            StatementUtils.setArguments(pstmt, args);
            return queryExecutor.execute(pstmt);
        } catch (SQLException e) {
            log.debug("ERROR CODE: {} SQL STATE: {}", e.getErrorCode(), e.getSQLState());
            throw new DataAccessException(e);
        }
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        List<T> result = query(sql, rowMapper, args);
        return DataAccessUtils.getSingleResult(result);
    }
}
