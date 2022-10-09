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
    private static final Object[] EMPTY_ARGUMENTS = new Object[0];

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private <T> T execute(final StatementExecutor<T> statementExecutor, final PreparedStatement pstmt) {
        return statementExecutor.execute(pstmt);
    }

    public int update(final String sql, final Object... args) {
        return update(new SimplePreparedStatementSetter(sql, args));
    }

    public int update(final PreparedStatementSetter pss) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = pss.createPreparedStatement(conn)) {
            return execute(new SimpleStatementExecutor<>(), pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    public <T> List<T> query(final SimpleResultSetExtractor<T> rse, final PreparedStatementSetter pss) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = pss.createPreparedStatement(conn)) {

            return execute((preparedStatement) -> {
                try (ResultSet rs = pstmt.executeQuery()) {
                    return rse.extract(rs);
                } catch (SQLException e) {
                    throw new DataAccessException(e);
                }
            }, pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper) {
        return query(new SimpleResultSetExtractor<>(rowMapper),
                new SimplePreparedStatementSetter(sql, EMPTY_ARGUMENTS));
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        List<T> result = query(new SimpleResultSetExtractor<>(rowMapper), new SimplePreparedStatementSetter(sql, args));

        if (result.size() != 1) {
            throw new RuntimeException();
        }
        return result.get(0);
    }
}
