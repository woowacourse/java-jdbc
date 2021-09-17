package com.techcourse.dao.jdbc.template;

import com.techcourse.dao.jdbc.template.exception.ExecuteQueryException;
import com.techcourse.dao.jdbc.template.exception.ResultSetCloseException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class JdbcTemplate<T> {

    private static final Logger LOG = LoggerFactory.getLogger(JdbcTemplate.class);
    private static final String EXECUTE_QUERY_EXCEPTION_MESSAGE = "executeQuery() 실행에 실패했습니다.";
    private static final String EXECUTE_UPDATE_EXCEPTION_MESSAGE = "executeUpdate() 실행에 실패했습니다.";
    private static final String RESULTSET_CLOSE_EXCEPTION_MESSAGE = "ResultSet close()에 실패했습니다.";

    private ResultSet executeQuery(PreparedStatement pstmt) throws SQLException {
        return pstmt.executeQuery();
    }

    protected abstract DataSource getDataSource();

    public T query(String sql, RowMapper<T> rowMapper, Object... params) {

        final DataSource dataSource = this.getDataSource();
        ResultSet rs = null;
        try (final Connection conn = dataSource.getConnection();
            final PreparedStatement pstmt = conn.prepareStatement(sql)) {

            PreparedStatementSetter.setValues(pstmt, params);
            rs = executeQuery(pstmt);
            LOG.debug("query : {}", sql);
            return rowMapper.mapRow(rs);
        } catch (SQLException e) {
            LOG.error(EXECUTE_QUERY_EXCEPTION_MESSAGE, e);
            throw new ExecuteQueryException(EXECUTE_QUERY_EXCEPTION_MESSAGE, e);
        } finally {
            tryCloseResultSet(rs);
        }
    }

    private void tryCloseResultSet(ResultSet rs) {
        try {
            closeResultSet(rs);
        } catch (SQLException e) {
            LOG.error(RESULTSET_CLOSE_EXCEPTION_MESSAGE, e);
            throw new ResultSetCloseException(RESULTSET_CLOSE_EXCEPTION_MESSAGE, e);
        }
    }

    private void closeResultSet(ResultSet rs) throws SQLException {
        if (rs != null) {
            rs.close();
        }
    }

    public void update(String sql, Object... params) {

        final DataSource dataSource = this.getDataSource();
        try (final Connection conn = dataSource.getConnection();
            final PreparedStatement pstmt = conn.prepareStatement(sql)) {

            PreparedStatementSetter.setValues(pstmt, params);
            LOG.debug("query : {}", sql);

            pstmt.executeUpdate();
        } catch (SQLException e) {
            LOG.error(EXECUTE_UPDATE_EXCEPTION_MESSAGE, e);
            throw new ExecuteQueryException(EXECUTE_QUERY_EXCEPTION_MESSAGE, e);
        }
    }
}
