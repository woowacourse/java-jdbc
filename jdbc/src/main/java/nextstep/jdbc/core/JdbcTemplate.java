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

    private <T> T execute(final PreparedStatementCreator psc, final PreparedStatementCallback<T> sc) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = psc.createPreparedStatement(conn)) {
            return sc.doInPreparedStatement(ps);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private <T> T query(final String sql, final ResultSetExtractor<T> rse) {
        return execute(new SimplePreparedStatementCreator(sql), ps -> {
            try (ResultSet rs = ps.executeQuery()) {
                return rse.extractData(rs);
            } catch (SQLException e) {
                log.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }
        });
    }

    private <T> T query(final String sql, final PreparedStatementSetter pss, final ResultSetExtractor<T> rse) {
        return execute(new SimplePreparedStatementCreator(sql), ps -> {
            pss.setValues(ps);
            try (ResultSet rs = ps.executeQuery()) {
                return rse.extractData(rs);
            } catch (SQLException e) {
                log.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }
        });
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper) {
        return query(sql, new RowMapperResultSetExtractor<>(rowMapper));
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        List<T> results = query(sql, new ArgPreparedStatementSetter(args),
                new RowMapperResultSetExtractor<>(rowMapper));
        return DataAccessUtils.nullableSingleResult(results);
    }

    private void update(final String sql, final PreparedStatementSetter pss) {
        execute(new SimplePreparedStatementCreator(sql), ps -> {
            pss.setValues(ps);
            return ps.executeUpdate();
        });
    }

    public void update(final String sql, final Object... args) {
        update(sql, new ArgPreparedStatementSetter(args));
    }
}
