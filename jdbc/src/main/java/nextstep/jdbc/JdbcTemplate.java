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

    public void update(final String sql, final Object... params) {
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            log.debug("query : {}", sql);

            setParameters(pstmt, params);
            pstmt.executeUpdate();
        } catch (final SQLException e) {
            throw new DataAccessException(e.getMessage());
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
            throw new DataAccessException(e.getMessage());
        }
        return null;
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... params) {
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            log.debug("query : {}", sql);

            setParameters(pstmt, params);
            final List<T> result = new ArrayList<>();
            final ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                result.add(rowMapper.rowMap(rs, rs.getRow()));
            }
            return result;
        } catch (final SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    private void setParameters(final PreparedStatement pstmt, final Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            pstmt.setObject(i + 1, params[i]);
        }
    }
}
