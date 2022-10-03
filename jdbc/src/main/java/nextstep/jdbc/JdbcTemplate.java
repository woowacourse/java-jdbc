package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.Nullable;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, @Nullable Object... args) throws DataAccessException {
        final List<T> results = query(sql, rowMapper, args);
        return DataAccessUtils.nullableSingleResult(results);
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, @Nullable Object... args) throws DataAccessException {
        ResultSet rs = null;

        try(
                final var conn = dataSource.getConnection();
                final var pstmt = conn.prepareStatement(sql)
        ) {
            setPstmt(pstmt, args);
            rs = pstmt.executeQuery();

            log.debug("query : {}", sql);

            return extractData(rowMapper, rs);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            closeResultSet(rs);
        }
    }

    private <T> List<T> extractData(final RowMapper<T> rowMapper, final ResultSet rs) throws SQLException {
        final List<T> results = new ArrayList<>();
        int rowNum = 0;

        while (rs.next()) {
            results.add(rowMapper.mapRow(rs, rowNum++));
        }
        return results;
    }

    private void setPstmt(final PreparedStatement pstmt, final Object[] args) throws SQLException {
        if (hasArgs(args)) {
            setValues(pstmt, args);
        }
    }

    private boolean hasArgs(final Object[] args) {
        return args != null;
    }

    private void setValues(final PreparedStatement pstmt, final Object[] args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            final var arg = args[i];
            pstmt.setObject(i + 1, arg);
        }
    }

    private void closeResultSet(final ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException e) {
            log.trace("Could not close JDBC ResultSet", e);
        }
    }
}
