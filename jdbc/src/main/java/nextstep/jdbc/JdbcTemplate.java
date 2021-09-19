package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import nextstep.jdbc.exception.ExecuteQueryException;
import nextstep.jdbc.preparedstatementsetter.ArgumentPreparedStatementSetter;
import nextstep.jdbc.preparedstatementsetter.PreparedStatementSetter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate<T> {

    private static final Logger LOG = LoggerFactory.getLogger(JdbcTemplate.class);
    private static final String EXECUTE_QUERY_EXCEPTION_MESSAGE = "executeQuery() 실행에 실패했습니다.";
    private static final String EXECUTE_UPDATE_EXCEPTION_MESSAGE = "executeUpdate() 실행에 실패했습니다.";

    private final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        if (dataSource == null) {
            throw new IllegalArgumentException("Property 'dataSource' is required");
        }
        this.dataSource = dataSource;
    }

    public T query(String sql, RowMapper<T> rowMapper, Object... params) {

        try (final Connection conn = dataSource.getConnection();
            final PreparedStatement pstmt = getPreparedStatement(sql, conn, params);
            final ResultSet rs = executeQuery(pstmt)) {

            LOG.debug("query : {}", sql);
            final RowMapperResultSetExtractor<T> rowMapperResultSetExtractor = new RowMapperResultSetExtractor<>(rowMapper);
            final List<T> results = rowMapperResultSetExtractor.extractData(rs);
            return results.get(0);

        } catch (SQLException e) {
            LOG.error(EXECUTE_QUERY_EXCEPTION_MESSAGE, e);
            throw new ExecuteQueryException(EXECUTE_QUERY_EXCEPTION_MESSAGE, e);
        }
    }

    public List<T> query(String sql, RowMapper<T> rowMapper) {

        try (final Connection conn = dataSource.getConnection();
            final PreparedStatement pstmt = conn.prepareStatement(sql);
            final ResultSet rs = executeQuery(pstmt)) {

            LOG.debug("query : {}", sql);
            final RowMapperResultSetExtractor<T> rowMapperResultSetExtractor = new RowMapperResultSetExtractor<>(rowMapper);
            return rowMapperResultSetExtractor.extractData(rs);

        } catch (SQLException e) {
            LOG.error(EXECUTE_QUERY_EXCEPTION_MESSAGE, e);
            throw new ExecuteQueryException(EXECUTE_QUERY_EXCEPTION_MESSAGE, e);
        }
    }

    private PreparedStatement getPreparedStatement(String sql, Connection conn, Object[] params) throws SQLException {
        final PreparedStatement pstmt = conn.prepareStatement(sql);
        final PreparedStatementSetter pstmtSetter = new ArgumentPreparedStatementSetter(params);
        pstmtSetter.setValues(pstmt);
        return pstmt;
    }

    private ResultSet executeQuery(PreparedStatement pstmt) throws SQLException {
        return pstmt.executeQuery();
    }

    public void update(String sql, Object... params) {

        try (final Connection conn = dataSource.getConnection();
            final PreparedStatement pstmt = getPreparedStatement(sql, conn, params)) {

            LOG.debug("query : {}", sql);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            LOG.error(EXECUTE_UPDATE_EXCEPTION_MESSAGE, e);
            throw new ExecuteQueryException(EXECUTE_QUERY_EXCEPTION_MESSAGE, e);
        }
    }
}
