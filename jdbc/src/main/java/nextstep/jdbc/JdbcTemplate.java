package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import nextstep.jdbc.exception.DataAccessException;
import nextstep.jdbc.utils.DataAccessUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T execute(final PreparedStatementCreator psc, final PreparedStatementCallback<T> action) throws DataAccessException {
        try (final Connection conn = dataSource.getConnection();
                final PreparedStatement pstmt = psc.createPreparedStatement(conn)) {
            return action.doInPreparedStatement(pstmt);
        } catch (SQLException e) {
            throw new DataAccessException("execute SQLException", e);
        }
    }

    public void execute(String sql) {
        execute(conn -> conn.prepareStatement(sql), PreparedStatement::execute);
    }

    public int update(final PreparedStatementCreator psc) {
        log.debug("Executing prepared SQL update");

        return execute(psc, PreparedStatement::executeUpdate);
    }

    public int update(final String sql, final Object... args) throws DataAccessException {
        return update(new PreparedStatementCreatorImpl(sql, args));
    }

    public <T> List<T> query(final PreparedStatementCreator psc, final ResultSetExtractor<List<T>> rse) throws DataAccessException {
        log.debug("Executing prepared SQL query");

        return execute(psc, ps -> {
            try (final ResultSet rs = ps.executeQuery()) {
                return rse.extractData(rs);
            } catch (SQLException e) {
                throw new DataAccessException("rse Exception", e);
            }
        });
    }

    public <T> List<T> query(final String sql, final ResultSetExtractor<List<T>> rse, final Object... args) throws DataAccessException {
        return query(new PreparedStatementCreatorImpl(sql, args), rse);
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) throws DataAccessException {
        return query(sql, new RowMapperResultSetExtractor<>(rowMapper), args);
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) throws DataAccessException {
        List<T> results = query(sql, rowMapper, args);
        return DataAccessUtils.nullableSingleResult(results);
    }

    private static class PreparedStatementCreatorImpl implements PreparedStatementCreator {
        private final String sql;
        private final Object[] args;

        public PreparedStatementCreatorImpl(String sql, Object[] args) {
            this.sql = sql;
            this.args = args;
        }

        @Override
        public PreparedStatement createPreparedStatement(Connection conn) throws SQLException {
            PreparedStatement pstmt = conn.prepareStatement(sql);

            for (int i = 0; i < args.length; i++) {
                pstmt.setObject(i + 1, args[i]);
            }
            return pstmt;
        }
    }
}
