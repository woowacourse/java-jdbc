package nextstep.jdbc;

import static java.sql.ResultSet.CONCUR_READ_ONLY;
import static java.sql.ResultSet.TYPE_SCROLL_INSENSITIVE;

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
    private static final String INCORRECT_RESULT_SIZE_MESSAGE = "Incorrect result size: expected 1, actual %d";

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(final String sql, final Object... args) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setArguments(pstmt, args);
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            log.debug("ERROR CODE: {} SQL STATE: {}", e.getErrorCode(), e.getSQLState());
            throw new DataAccessException(e);
        }
    }

    public <T> List<T> query(final String sql, RowMapper<T> rowMapper, Object... args) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setArguments(pstmt, args);
            return mapResultSetToList(rowMapper, pstmt.executeQuery());
        } catch (SQLException e) {
            log.debug("ERROR CODE: {} SQL STATE: {}", e.getErrorCode(), e.getSQLState());
            throw new DataAccessException(e);
        }
    }

    public <T> T queryForObject(final String sql, RowMapper<T> rowMapper, Object... args) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, TYPE_SCROLL_INSENSITIVE, CONCUR_READ_ONLY)) {
            setArguments(pstmt, args);
            ResultSet rs = pstmt.executeQuery();
            validateResultSetSize(rs);
            return mapResultSetToObject(rowMapper, rs);
        } catch (SQLException e) {
            log.debug("ERROR CODE: {} SQL STATE: {}", e.getErrorCode(), e.getSQLState());
            throw new DataAccessException(e);
        }
    }

    private static void setArguments(final PreparedStatement pstmt, final Object[] args) throws SQLException {
        int parameterIndex = 1;
        for (Object arg : args) {
            pstmt.setObject(parameterIndex++, arg);
        }
    }

    private static <T> List<T> mapResultSetToList(final RowMapper<T> rowMapper, final ResultSet rs)
            throws SQLException {
        List<T> result = new ArrayList<>();
        while (rs.next()) {
            result.add(rowMapper.mapRow(rs, rs.getRow()));
        }
        return result;
    }

    private static int getResultSetSize(final ResultSet rs) throws SQLException {
        rs.last();
        int size = rs.getRow();
        rs.beforeFirst();
        return size;
    }

    private static void validateResultSetSize(final ResultSet rs) throws SQLException {
        int size = getResultSetSize(rs);
        if (size != 1) {
            throw new DataAccessException(String.format(INCORRECT_RESULT_SIZE_MESSAGE, size));
        }
    }

    private static <T> T mapResultSetToObject(final RowMapper<T> rowMapper, final ResultSet rs) throws SQLException {
        rs.next();
        return rowMapper.mapRow(rs, 1);
    }

    public DataSource getDataSource() {
        return dataSource;
    }
}
