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

    private <T> T query(final String sql, final PreparedStatementSetter pss, final ResultSetExtractor<T> rse) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            pss.setValues(ps);
            ResultSet rs = ps.executeQuery();
            return rse.extractData(rs);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private <T> T query(final String sql, final ResultSetExtractor<T> rse) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            log.debug("query : {}", sql);

            return rse.extractData(rs);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper) {
        return query(sql, new RowMapperResultSetExtractor<>(rowMapper));
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        List<T> results = query(sql, new ArgPreparedStatementSetter(args),
                new RowMapperResultSetExtractor<>(rowMapper));
        return DataAccessUtils.nullableSingleResult(results);
    }

    public void update(final String sql, final Object... args) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);

            setPreparedStatement(ps, args);
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private void setPreparedStatement(final PreparedStatement ps, final Object[] args) throws SQLException {
        int argIdx = 0;
        for (Object arg : args) {
            argIdx++;
            if (arg instanceof String) {
                ps.setString(argIdx, (String) arg);
                continue;
            }
            if (arg instanceof Long) {
                ps.setLong(argIdx, (Long) arg);
                continue;
            }
            if (arg instanceof Integer) {
                ps.setLong(argIdx, (Integer) arg);
            }
        }
    }
}
