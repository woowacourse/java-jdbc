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
import org.springframework.lang.Nullable;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, @Nullable final Object... args) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = createPreparedStatement(conn, sql, args);
             ResultSet rs = pstmt.executeQuery()) {
            final List<T> results = new ArrayList<>();
            int rowNum = 0;
            while (rs.next()) {
                rowNum += 1;
                final T row = rowMapper.mapToRow(rs, rowNum);
                results.add(row);
            }
            log.debug("query : {}", sql);
            return results;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, @Nullable final Object... args) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = createPreparedStatement(conn, sql, args);
             ResultSet rs = pstmt.executeQuery()) {
            final int rowCount = rs.getRow();
            if (rowCount > 1) {
                throw new DataAccessException("1개보다 많은 값이 존재합니다.");
            }
            if (rs.next()) {
                log.debug("query : {}", sql);
                return rowMapper.mapToRow(rs, 1);
            }
            return null;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public int update(final String sql, @Nullable final Object... args) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = createPreparedStatement(conn, sql, args)) {
            log.debug("query : {}", sql);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private PreparedStatement createPreparedStatement(final Connection conn, final String sql,
                                                      @Nullable final Object[] args) throws SQLException {
        final PreparedStatement pstmt = conn.prepareStatement(sql);
        int statementNum = 1;
        if (args == null) {
            return pstmt;
        }
        for (Object obj : args) {
            pstmt.setObject(statementNum, obj);
            statementNum += 1;
        }
        return pstmt;
    }
}
