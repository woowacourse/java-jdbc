package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... parameters) {
        QueryExecutor<List<T>> queryExecutor = (pstmt) -> {
            try (ResultSet rs = pstmt.executeQuery()) {
                List<T> result = new ArrayList<>();
                int rowNum = 1;

                while (rs.next()) {
                    result.add(rowMapper.mapRow(rs, rowNum));
                }

                return result;
            }
        };

        return executeQuery(queryExecutor, sql, parameters);
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... parameters) {
        List<T> result = query(sql, rowMapper, parameters);

        if (result.isEmpty()) {
            throw new DataAccessException("쿼리 결과가 존재하지 않습니다.");
        }

        if (result.size() > 1) {
            throw new DataAccessException("쿼리 결과가 2개 이상입니다.");
        }

        return result.get(0);
    }

    public void update(String sql, Object... parameters) {
        QueryExecutor queryExecutor = (pstmt) -> pstmt.executeUpdate();

        executeQuery(queryExecutor, sql, parameters);
    }

    public void update(Connection connection, String sql, Object... parameters) {
        QueryExecutor queryExecutor = (pstmt) -> pstmt.executeUpdate();

        executeQuery(connection, queryExecutor, sql, parameters);
    }

    private <T> T executeQuery(QueryExecutor<T> queryExecutor, String sql, Object... parameters) {
        try (
            Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            log.debug("query : {}", sql);
            bindParameters(pstmt, parameters);
            return queryExecutor.execute(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private <T> T executeQuery(Connection connection, QueryExecutor<T> queryExecutor, String sql,
        Object... parameters) {
        try (
            PreparedStatement pstmt = connection.prepareStatement(sql)
        ) {
            log.debug("query : {}", sql);
            bindParameters(pstmt, parameters);
            return queryExecutor.execute(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private void bindParameters(PreparedStatement pstmt, Object... parameters) throws SQLException {
        for (int i = 0; i < parameters.length; i++) {
            pstmt.setObject(i + 1, parameters[i]);
        }
    }

}
