package nextstep.jdbc;

import nextstep.jdbc.exception.JdbcTemplateException;
import nextstep.jdbc.executor.PreparedStatementExecutor;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class JdbcTemplate {

    private final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> List<T> query(String sql, RowMapper<T> mapper, Object... params) {
        return execute(sql, pstmt -> PreparedStatementExecutor.query(mapper, pstmt, params));
    }

    public <T> T queryForObject(String sql, RowMapper<T> mapper, Object... params) {
        return execute(sql, pstmt -> PreparedStatementExecutor.queryForObject(mapper, pstmt, params));
    }

    public int update(String sql, Object... params) {
        return execute(sql, pstmt -> PreparedStatementExecutor.update(pstmt, params));
    }

    private <T> T execute(String sql, QueryCallBack<T> queryCallBack) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            return queryCallBack.execute(pstmt);
        } catch (SQLException e) {
            throw new JdbcTemplateException();
        }
    }
}
