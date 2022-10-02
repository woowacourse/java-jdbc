package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(final String sql, final Object... params) {
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            log.debug("query : {}", sql);

            setParameters(pstmt, params);
            pstmt.executeUpdate();
        } catch (final SQLException e) {
            throw new RuntimeException("커넥션 연결에 실패했습니다.");
        }
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... params) {
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            log.debug("query : {}", sql);

            setParameters(pstmt, params);
            final ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rowMapper.rowMap(rs, rs.getRow());
            }
        } catch (final SQLException e) {
            throw new RuntimeException("커넥션 연결에 실패했습니다.");
        }
        return null;
    }

    private void setParameters(final PreparedStatement pstmt, final Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            pstmt.setObject(i + 1, params[i]);
        }
    }
}
