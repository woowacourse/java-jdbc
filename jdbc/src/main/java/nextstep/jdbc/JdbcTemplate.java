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

    public void update(final String sql, final Object... parameters) {
        log.debug("query : {}", sql);
        execute(sql, ps -> {
            setPreparedStatement(ps, parameters);
            return ps.executeUpdate();
        });
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... parameters) {
        log.debug("query : {}", sql);
        return execute(sql, ps -> {
            setPreparedStatement(ps, parameters);
            ResultSet rs = ps.executeQuery();
            return extractData(rowMapper, rs);
        });
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... parameters) {
        List<T> results = query(sql, rowMapper, parameters);
        return singleResult(results);
    }

    private <T> T execute(final String sql, final PreparedStatementCallback<T> action) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            return action.doInPreparedStatement(ps);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private void setPreparedStatement(final PreparedStatement ps, final Object[] parameters) throws SQLException {
        for (int i = 0; i < parameters.length; i++) {
            ps.setObject(i + 1, parameters[i]);
        }
    }

    private <T> List<T> extractData(final RowMapper<T> rowMapper, final ResultSet rs) throws SQLException {
        List<T> result = new ArrayList<>();
        while (rs.next()) {
            result.add(rowMapper.mapRow(rs));
        }
        rs.close();
        return result;
    }

    private <T> T singleResult(final List<T> results) {
        if (results.isEmpty()) {
            throw new DataAccessException("no results found.");
        }
        if (results.size() > 1) {
            throw new DataAccessException("multiple results found.");
        }
        return results.get(0);
    }
}
