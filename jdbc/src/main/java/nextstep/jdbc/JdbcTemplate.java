package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import javax.sql.DataSource;
import nextstep.jdbc.core.PreparedStatementCallback;
import nextstep.jdbc.core.RowMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(String sql, final Object... args) {
        execute(sql, pstmt -> {
            setArguments(pstmt, args);
            log.debug("query : {}", sql);
            return pstmt.executeUpdate();
        });
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        return execute(sql, pstmt -> {
            setArguments(pstmt, args);
            ResultSet rs = pstmt.executeQuery();
            log.debug("query : {}", sql);
            if (rs.next()) {
                return rowMapper.mapRow(rs, rs.getRow());
            }
            return null;
        });
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper) {
        return execute(sql, pstmt -> {
            ResultSet rs = pstmt.executeQuery();
            log.debug("query : {}", sql);
            return extractData(rs, rowMapper);
        });
    }

    private void setArguments(PreparedStatement pstmt, Object... args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            pstmt.setObject(i + 1, args[i]);
        }
    }

    private <T> T execute(final String sql, PreparedStatementCallback<T> callback) {
        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement ptsmt = conn.prepareStatement(sql);
            return callback.doInPreparedStatement(ptsmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    private <T> List<T> extractData(ResultSet rs, RowMapper<T> rowMapper) throws SQLException {
        List<T> results = new ArrayList<>();
        while (rs.next()) {
            results.add(rowMapper.mapRow(rs, rs.getRow()));
        }
        return results;
    }
}
