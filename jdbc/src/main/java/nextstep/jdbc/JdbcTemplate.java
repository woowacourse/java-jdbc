package nextstep.jdbc;

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

    public void execute(final String sql, final Object... params) {
        log.debug("query : {}", sql);
        try (final var conn = dataSource.getConnection();
             final var pstmt = conn.prepareStatement(sql)
        ) {
            setPstmtParams(pstmt, params);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    public <T> T queryOne(final String sql,
                          final ThrowingFunction<ResultSet, T, SQLException> rowMapper,
                          final Object... conditionParams) {

        log.debug("query : {}", sql);
        ResultSet rs = null;
        try (final var conn = dataSource.getConnection();
             final var pstmt = conn.prepareStatement(sql)
        ) {
            setPstmtParams(pstmt, conditionParams);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rowMapper.apply(rs);
            }
            return null;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        } finally {
            closeResultSet(rs);
        }
    }

    public <T> List<T> queryAll(final String sql,
                                final ThrowingFunction<ResultSet, T, SQLException> userRowMapper) {
        log.debug("query : {}", sql);
        ResultSet rs = null;
        try (final var conn = dataSource.getConnection();
             final var pstmt = conn.prepareStatement(sql)
        ) {
            rs = pstmt.executeQuery();
            List<T> resultRows = new ArrayList<>();
            while (rs.next()) {
                final T resultRow = userRowMapper.apply(rs);
                resultRows.add(resultRow);
            }
            return resultRows;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        } finally {
            closeResultSet(rs);
        }
    }

    private void setPstmtParams(final PreparedStatement pstmt, final Object... params) throws SQLException {
        for (int i = 0; i < List.of(params).size(); i++) {
            pstmt.setObject(i + 1, params[i]);
        }
    }

    private void closeResultSet(final ResultSet rs) {
        try {
            rs.close();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }
}
