package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

    public int update(String sql, Object... parameters) {
        return execute(sql, ps -> {
            setPreparedStatement(ps, parameters);
            return ps.executeUpdate();
        });
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... parameters) {
        return execute(sql, ps -> {
            setPreparedStatement(ps, parameters);
            ResultSet resultSet = ps.executeQuery();

            List<T> extracted = RowMapperResultSetExtractor.extractData(resultSet, rowMapper);
            return extracted.stream()
                    .findFirst()
                    .orElseThrow(DataAccessException::new);
        });
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper) {
        return execute(sql, ps -> {
            ResultSet resultSet = ps.executeQuery();

            return RowMapperResultSetExtractor.extractData(resultSet, rowMapper);
        });
    }

    private <T> T execute(String sql, PreparedStatementCallback<T> action) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            return action.doInPreparedStatement(ps);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private void setPreparedStatement(PreparedStatement ps, Object... parameters) throws SQLException {
        for (int i = 1; i <= parameters.length; i++) {
            ps.setObject(i, parameters[i - 1]);
        }
    }
}
