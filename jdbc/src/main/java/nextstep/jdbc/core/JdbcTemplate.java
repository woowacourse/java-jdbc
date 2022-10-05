package nextstep.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import nextstep.dao.DataAccessUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            log.debug("query : {}", sql);

            RowMapperResultSetExtractor<T> rowMapperResultSetExtractor = new RowMapperResultSetExtractor<>(rowMapper);
            return rowMapperResultSetExtractor.extractData(rs);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        ResultSet rs = null;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);

            setPreparedStatement(ps, args);
            rs = ps.executeQuery();

            RowMapperResultSetExtractor<T> rowMapperResultSetExtractor = new RowMapperResultSetExtractor<>(rowMapper);
            return DataAccessUtils.nullableSingleResult(rowMapperResultSetExtractor.extractData(rs));
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException ignored) {
            }
        }
    }

    public void update(final String sql, final Object... args) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);

            setPreparedStatement(pstmt, args);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private void setPreparedStatement(final PreparedStatement pstmt, final Object[] args) throws SQLException {
        int argIdx = 0;
        for (Object arg : args) {
            argIdx++;
            if (arg instanceof String) {
                pstmt.setString(argIdx, (String) arg);
                continue;
            }
            if (arg instanceof Long) {
                pstmt.setLong(argIdx, (Long) arg);
                continue;
            }
            if (arg instanceof Integer) {
                pstmt.setLong(argIdx, (Integer) arg);
            }
        }
    }
}
