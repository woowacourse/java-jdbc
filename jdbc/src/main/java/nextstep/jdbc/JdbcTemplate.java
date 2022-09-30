package nextstep.jdbc;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(final String sql, final PreparedStatementSetter pss) {
        return execute(sql, ps -> {
            pss.setValues(ps);
            return ps.executeUpdate();
        });
    }

    public <T> Optional<T> queryForObject(final String sql, final RowMapper<T> rowMapper, final PreparedStatementSetter pss) {
        return execute(sql, ps -> {
            pss.setValues(ps);
            try (var rs = ps.executeQuery()) {
                return new SingleResultSetExtractor<T>(rowMapper).extractData(rs);
            } catch (SQLException e) {
                log.error("Error in executing SQL statement: {}", sql, e);
                throw new DataAccessException(e);
            }
        });
    }

    public <T> List<T> queryForList(final String sql, final RowMapper<T> rowMapper) {
        return execute(sql, ps -> {
            try (var rs = ps.executeQuery()) {
                return new MultipleResultSetExtractor<T>(rowMapper).extractData(rs);
            } catch (SQLException e) {
                log.error("Error in executing SQL statement: {}", sql, e);
                throw new DataAccessException(e);
            }
        });
    }

    private <T> T execute(String sql, PreparedStatementCallback<T> action) {
        try (var conn = dataSource.getConnection();
             var ps = conn.prepareStatement(sql)) {
            return action.doInPreparedStatement(ps);
        } catch (SQLException e) {
            log.error("Error in executing SQL statement: {}", sql, e);
            throw new DataAccessException(e);
        }
    }
}
