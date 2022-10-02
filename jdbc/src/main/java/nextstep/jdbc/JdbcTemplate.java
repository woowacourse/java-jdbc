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

    public void update(final String sql, final PreparedStatementSetter ps) {
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            log.debug("query : {}", sql);
            ps.set(pstmt);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper) {
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            log.debug("query : {}", sql);
            return execute(pstmt, rowMapper);

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }


    public <T> T queryForObject(final String sql, final PreparedStatementSetter ps, final RowMapper<T> rowMapper) {
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            log.debug("query : {}", sql);
            ps.set(pstmt);

            final List<T> results = execute(pstmt, rowMapper);
            return nullableSingleResult(results);

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private <T> List<T> execute(final PreparedStatement pstmt, final RowMapper<T> rowMapper) {
        try (final ResultSet rs = pstmt.executeQuery()) {
            return extract(rs, rowMapper);

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private <T> List<T> extract(final ResultSet rs, final RowMapper<T> rowMapper) throws SQLException {
        final List<T> results = new ArrayList<>();
        while (rs.next()) {
            final T result = rowMapper.mapTow(rs);
            results.add(result);
        }
        return results;
    }

    private <T> T nullableSingleResult(List<T> results) {
        if (results == null || results.isEmpty()) {
            throw new IncorrectResultSizeDataAccessException(1, 0);
        }
        if (results.size() > 1) {
            throw new IncorrectResultSizeDataAccessException(1, results.size());
        }
        return results.iterator().next();
    }
}
