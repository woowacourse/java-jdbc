package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DataSourceUtils;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);
    private static final Object[] EMPTY_ARGUMENTS = new Object[0];

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private <T> T execute(final PreparedStatementSetter pss, final StatementExecutor<T> se) {
        Connection conn = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement pstmt = pss.createPreparedStatement(conn)) {
            return se.execute(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    public int update(final PreparedStatementSetter pss) {
        return execute(pss, new UpdateStatementExecutor<>());
    }

    public int update(final String sql, final Object... args) {
        return update(new SimplePreparedStatementSetter(sql, args));
    }

    public <T> List<T> query(final PreparedStatementSetter pss, final ResultSetExtractor<T> rse) {
        return execute(pss, (pstmt) -> {
            try (ResultSet rs = pstmt.executeQuery()) {
                return rse.extract(rs);
            } catch (SQLException e) {
                throw new DataAccessException(e);
            }
        });
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper) {
        return query(new SimplePreparedStatementSetter(sql, EMPTY_ARGUMENTS),
                new SimpleResultSetExtractor<>(rowMapper));
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        List<T> result = query(new SimplePreparedStatementSetter(sql, args), new SimpleResultSetExtractor<>(rowMapper));
        return DataAccessUtils.nullableSingleResult(result);
    }
}
