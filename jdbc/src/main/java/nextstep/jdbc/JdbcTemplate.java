package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import nextstep.jdbc.exception.IncorrectResultSizeDataAccessException;
import nextstep.jdbc.exception.JdbcTemplateException;

public class JdbcTemplate {

    private final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> List<T> query(String sql, RowMapper<T> mapper, Object... params) {
        return execute(sql, pstmt -> query(pstmt, mapper, params));
    }

    public static <T> List<T> query(PreparedStatement pstmt, RowMapper<T> mapper, Object[] params) throws SQLException {
        setArguments(pstmt, params);
        try (ResultSet resultSet = pstmt.executeQuery()) {
            List<T> results = new ArrayList<>();
            while (resultSet.next()) {
                results.add(mapper.mapRow(resultSet));
            }
            return results;
        }
    }

    public <T> T queryForObject(String sql, RowMapper<T> mapper, Object... params) {
        return execute(sql, pstmt -> queryForObject(pstmt, mapper, params));
    }

    public <T> T queryForObject(PreparedStatement pstmt, RowMapper<T> mapper, Object[] params) throws SQLException {
        List<T> results = query(pstmt, mapper, params);
        if (results.size() != 1) {
            throw new IncorrectResultSizeDataAccessException(1, results.size());
        }
        return results.get(0);
    }

    public int update(String sql, Object... params) {
        return execute(sql, pstmt -> update(pstmt, params));
    }

    public static int update(PreparedStatement pstmt, Object[] params) throws SQLException {
        setArguments(pstmt, params);
        return pstmt.executeUpdate();
    }

    private <T> T execute(String sql, QueryCallBack<T> queryCallBack) {
        try (Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            return queryCallBack.execute(pstmt);
        } catch (SQLException e) {
            throw new JdbcTemplateException();
        }
    }

    private static void setArguments(PreparedStatement pstmt, Object[] params) throws SQLException {
        for (int index = 0; index < params.length; index++) {
            pstmt.setObject(index + 1, params[index]);
        }
    }
}
