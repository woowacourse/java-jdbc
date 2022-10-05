package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(final String sql, final Object... parameters) {
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setParameters(pstmt, parameters);

            log.debug("query : {}", sql);

            pstmt.executeUpdate();
        } catch (final SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... parameters) {
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setParameters(pstmt, parameters);

            log.debug("query : {}", sql);

            return execute(pstmt, rowMapper);
        } catch (final SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... parameters) {
        try (final Connection conn = dataSource.getConnection();
             final PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setParameters(pstmt, parameters);

            log.debug("query : {}", sql);

            return getSingleRow(execute(pstmt, rowMapper));
        } catch (final SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private void setParameters(final PreparedStatement pstmt, final Object[] parameters) throws SQLException {
        for (int i = 0; i < parameters.length; i++) {
            pstmt.setObject(i + 1, parameters[i]);
        }
    }

    private <T> List<T> execute(final PreparedStatement pstmt, final RowMapper<T> rowMapper) throws SQLException {
        final ResultSetExtractor<List<T>> resultSetExtractor = new RowMapperResultSetExtractor<>(rowMapper);

        return resultSetExtractor.extractData(pstmt.executeQuery());
    }

    private <T> T getSingleRow(final List<T> results) {
        validateExecuteResultSize(results);

        return results.get(0);
    }

    private <T> void validateExecuteResultSize(final List<T> results) {
        if (results.isEmpty()) {
            throw new DataAccessException("데이터가 없습니다.");
        }
        if (results.size() >= 2) {
            throw new DataAccessException("데이터가 한개가 아닙니다. 데이터 수 : " + results.size());
        }
    }
}
