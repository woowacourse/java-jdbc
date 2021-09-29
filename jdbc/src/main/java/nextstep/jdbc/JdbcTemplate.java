package nextstep.jdbc;

import nextstep.jdbc.exception.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private static void logSql(String sql) {
        log.debug("query : {}", sql);
    }

    private final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> List<T> queryMany(String sql, RowMapper<T> rowMapper, Object... args) {
        ResultExtractCallBack<List<T>> resultExtractCallBack = rs -> {
            List<T> results = new ArrayList<>();
            while (rs.next()) {
                results.add(rowMapper.mapRow(rs));
            }
            return results;
        };

        return query(sql, resultExtractCallBack, args);
    }

    public <T> T queryOne(String sql, RowMapper<T> rowMapper, Object... args) {
        List<T> results = queryMany(sql, rowMapper, args);

        if (results.isEmpty()) {
            throw new DataAccessException("데이터가 존재하지 않음.");
        }
        if (results.size() > 1) {
            throw new DataAccessException("데이터가 1개보다 많음.");
        }

        return results.get(0);
    }

    public <T> T query(String sql, ResultExtractCallBack<T> resultExtractCallBack, Object... args) {
        PreparedStatementCallback<T> preparedStatementCallback = pstmt -> {
            bindArguments(pstmt, args);
            try (ResultSet rs = pstmt.executeQuery()) {
                return resultExtractCallBack.extract(rs);
            }
        };

        return execute(sql, preparedStatementCallback);
    }

    public int update(String sql, Object... args) {
        logSql(sql);

        return execute(sql, pstmt -> {
            bindArguments(pstmt, args);
            return pstmt.executeUpdate();
        });
    }

    public <T> T execute(String sql, PreparedStatementCallback<T> preparedStatementCallback) {
        logSql(sql);

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            return preparedStatementCallback.run(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private void bindArguments(PreparedStatement pstmt, Object[] args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            pstmt.setObject(i + 1, args[i]);
        }
    }
}
