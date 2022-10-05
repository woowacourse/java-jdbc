package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

public class JdbcTemplate {

    private final JdbcExecutor jdbcExecutor;

    public JdbcTemplate(final DataSource dataSource) {
        this.jdbcExecutor = new JdbcExecutor(dataSource);
    }

    public int update(final String sql, final Object... args) {
        return jdbcExecutor.execute(sql, args, PreparedStatement::executeUpdate);
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) throws DataAccessException {
        return jdbcExecutor.execute(sql, args, (pstmt) -> {
            ResultSet rs = pstmt.executeQuery();
            return createResultByRowMapper(rowMapper, rs);
        });
    }

    private <T> List<T> createResultByRowMapper(final RowMapper<T> rowMapper, final ResultSet rs) throws SQLException {
        List<T> result = new ArrayList<>();
        while (rs.next()) {
            result.add(rowMapper.mapRow(rs));
        }
        rs.close();

        return result;
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) throws DataAccessException {
        List<T> results = query(sql, rowMapper, args);
        validateResultSizeSingle(results);

        return results.iterator().next();
    }

    private <T> void validateResultSizeSingle(final List<T> results) {
        if (results.isEmpty()) {
            throw new DataAccessException("Empty result");
        }
        if (results.size() > 1) {
            throw new DataAccessException("Incorrect result size, expected : 1, actual : " + results.size());
        }
    }
}
