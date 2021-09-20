package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import javax.sql.DataSource;
import nextstep.jdbc.exception.ExecuteQueryException;
import nextstep.jdbc.exception.IncorrectResultSizeDataAccessException;
import nextstep.jdbc.preparedstatementsetter.ArgumentPreparedStatementSetter;
import nextstep.jdbc.preparedstatementsetter.PreparedStatementSetter;
import nextstep.jdbc.rowmapper.RowMapper;
import nextstep.jdbc.rowmapper.RowMapperResultSetExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger LOG = LoggerFactory.getLogger(JdbcTemplate.class);
    private static final String EXECUTE_QUERY_EXCEPTION_MESSAGE = "executeQuery() 실행에 실패했습니다.";
    private static final String EXECUTE_UPDATE_EXCEPTION_MESSAGE = "executeUpdate() 실행에 실패했습니다.";

    private final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        if (Objects.isNull(dataSource)) {
            throw new IllegalArgumentException("Property 'dataSource' is required");
        }
        this.dataSource = dataSource;
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... params) {
        final List<T> results = this.query(sql, rowMapper, params);

        if (results.size() > 1) {
            throw new IncorrectResultSizeDataAccessException("query 결과가 1개가 아닌, " + results.size() + "개 입니다.");
        }
        if (results.isEmpty()) {
            return null;
        }
        return results.get(0);
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... params) {
        try (final Connection conn = dataSource.getConnection();
            final PreparedStatement pstmt = getPreparedStatement(sql, conn, params);
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
