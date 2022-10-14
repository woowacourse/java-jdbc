package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
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
        return execute(sql, ps -> {
            for (int i = 0; i < args.length; i++) {
                ps.setObject(i + 1, args[i]);
            }
            return ps.executeUpdate();
        });
    }

    public <T> Optional<T> queryForObject(final String sql, final RowMapper<T> rowMapper,
                                          final PreparedStatementSetter pss) {
        return execute(sql, ps -> {
            pss.setValues(ps);
            return getSingleOrEmpty(extractResults(sql, rowMapper, ps));
        });
    }

    public <T> List<T> queryForList(final String sql, final RowMapper<T> rowMapper) {
        return execute(sql, ps -> extractResults(sql, rowMapper, ps));
    }

    private <T> T execute(String sql, PreparedStatementCallback<T> action) {

        try (var conn = DataSourceUtils.getConnection(dataSource);
             var ps = conn.prepareStatement(sql)) {
            return action.doInPreparedStatement(ps);
        } catch (SQLException e) {
            log.error("Error in executing SQL statement: {}", sql, e);
            throw new DataAccessException(e);
        }
    }

    private <T> List<T> extractResults(final String sql, final RowMapper<T> rowMapper, final PreparedStatement ps) {
        try (var rs = ps.executeQuery()) {
            return new ResultSetExtractor<T>(rowMapper).extractData(rs);
        } catch (SQLException e) {
            log.error("Error in executing SQL statement: {}", sql, e);
            throw new DataAccessException(e);
        }
    }

    private <T> Optional<T> getSingleOrEmpty(final List<T> results) {
        if (results.size() == 0) {
            return Optional.empty();
        }
        return Optional.of(results.get(0));
    }
}
