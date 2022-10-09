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

    public int update(String sql, Object... args) {
        return execute(sql, pstmt -> pstmt.executeUpdate(), args);
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args)
            throws DataAccessException {
        return execute(sql, pstmt -> getResultSet(rowMapper, pstmt), args);
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return execute(sql, pstmt -> getResult(rowMapper, pstmt.executeQuery()), args);
    }

    private <T> T execute(final String sql, Executor<T> executor, Object[] args) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            setSqlParameters(pstmt, args);
            return executor.execute(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private void setSqlParameters(PreparedStatement pstmt, Object[] args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            if (args[i].getClass().equals(String.class)) {
                pstmt.setString(i + 1, (String) args[i]);
            } else if (args[i].getClass().equals(Long.class)) {
                pstmt.setLong(i + 1, (Long) args[i]);
            }
        }
    }

    private <T> T getResultSet(RowMapper<T> rowMapper, PreparedStatement pstmt) throws SQLException {
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
            return rowMapper.mapRow(rs, 0);
        }
        return null;
    }

    private <T> List<T> getResult(RowMapper<T> rowMapper, ResultSet rs) throws SQLException {
        int rowNum = 0;
        List<T> result = new ArrayList<>();
        while (rs.next()) {
            result.add(rowMapper.mapRow(rs, rowNum++));
        }
        return result;
    }
}
