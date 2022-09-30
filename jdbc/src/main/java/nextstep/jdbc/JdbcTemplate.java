package nextstep.jdbc;

import java.sql.SQLException;
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
        try (var conn = dataSource.getConnection();
             var ps = conn.prepareStatement(sql)) {
            log.info("Executing SQL: {}", sql);

            pss.setValues(ps);
            return ps.executeUpdate();
        } catch (SQLException e) {
            log.error("Error in executing SQL statement: {}", sql, e);
            throw new DataAccessException(e);
        }
    }
}
