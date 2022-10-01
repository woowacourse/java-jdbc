package nextstep.jdbc;

import static java.sql.ResultSet.CONCUR_READ_ONLY;
import static java.sql.ResultSet.TYPE_SCROLL_INSENSITIVE;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import nextstep.jdbc.support.DataAccessUtils;
import nextstep.jdbc.support.StatementUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(final String sql, final Object... args) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            StatementUtils.setArguments(pstmt, args);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            log.debug("ERROR CODE: {} SQL STATE: {}", e.getErrorCode(), e.getSQLState());
            throw new DataAccessException(e);
        }
    }

    public <T> List<T> query(final String sql, RowMapper<T> rowMapper, Object... args) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            StatementUtils.setArguments(pstmt, args);
            return DataAccessUtils.mapResultSetToList(rowMapper, pstmt.executeQuery());
        } catch (SQLException e) {
            log.debug("ERROR CODE: {} SQL STATE: {}", e.getErrorCode(), e.getSQLState());
            throw new DataAccessException(e);
        }
    }

    public <T> T queryForObject(final String sql, RowMapper<T> rowMapper, Object... args) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, TYPE_SCROLL_INSENSITIVE, CONCUR_READ_ONLY)) {
            StatementUtils.setArguments(pstmt, args);
            ResultSet rs = pstmt.executeQuery();
            DataAccessUtils.validateResultSetSize(rs);
            return DataAccessUtils.mapResultSetToObject(rowMapper, rs);
        } catch (SQLException e) {
            log.debug("ERROR CODE: {} SQL STATE: {}", e.getErrorCode(), e.getSQLState());
            throw new DataAccessException(e);
        }
    }

    public DataSource getDataSource() {
        return dataSource;
    }
}
