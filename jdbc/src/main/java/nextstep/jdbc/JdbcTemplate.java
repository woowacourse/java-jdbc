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
import org.springframework.jdbc.datasource.DataSourceUtils;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(final String sql, final Object... args) {
        return execute(sql, PreparedStatement::executeUpdate, args);
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return execute(sql, pstmt -> getResult(rowMapper, pstmt), args);
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return execute(sql, pstmt -> getResults(rowMapper, pstmt.executeQuery()), args);
    }

    private <T> T execute(final String sql, final Executor<T> executor, final Object... args) {
        Connection conn = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            setSqlParameters(pstmt, args);
            return executor.execute(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private void setSqlParameters(final PreparedStatement pstmt, final Object[] args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            pstmt.setObject(i + 1, args[i]);
        }
    }

    private <T> T getResult(final RowMapper<T> rowMapper, final PreparedStatement pstmt) throws SQLException {
        ResultSet rs = pstmt.executeQuery();
        List<T> results = getResults(rowMapper, rs);
        validateData(results);
        return results.get(0);
    }

    private <T> List<T> getResults(final RowMapper<T> rowMapper, final ResultSet rs) throws SQLException {
        List<T> result = new ArrayList<>();
        while (rs.next()) {
            result.add(rowMapper.mapRow(rs));
        }
        return result;
    }

    private <T> void validateData(List<T> result) {
        if (result.isEmpty()) {
            log.error("no data found");
            throw new DataAccessException();
        }
        if (result.size() > 1) {
            log.error("more than 1 data found");
            throw new DataAccessException();
        }
    }
}
