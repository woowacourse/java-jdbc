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

    public int update(final String sql, final Object... args) {
        return execute(sql, pstmt -> {
            setSqlParameters(pstmt, args);
            return pstmt.executeUpdate();
        });
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return execute(sql, pstmt -> {
            setSqlParameters(pstmt, args);
            return getResultSet(rowMapper, pstmt);
        });
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper) {
        return execute(sql, pstmt -> getResult(rowMapper, pstmt.executeQuery()));
    }

    private <T> T execute(final String sql, final Executor<T> executor) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            return executor.execute(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private void setSqlParameters(final PreparedStatement pstmt, final Object[] args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            if (args[i].getClass().equals(String.class)) {
                pstmt.setString(i + 1, (String) args[i]);
            } else if (args[i].getClass().equals(Long.class)) {
                pstmt.setLong(i + 1, (Long) args[i]);
            }
        }
    }

    private <T> T getResultSet(final RowMapper<T> rowMapper, final PreparedStatement pstmt) throws SQLException {
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
            return rowMapper.mapRow(rs, 0);
        }
        return null;
    }

    private <T> List<T> getResult(final RowMapper<T> rowMapper, final ResultSet rs) throws SQLException {
        int rowNum = 0;
        List<T> result = new ArrayList<>();
        while (rs.next()) {
            result.add(rowMapper.mapRow(rs, rowNum++));
        }
        return result;
    }
}
