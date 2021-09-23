package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate extends AbstractJdbcTemplate{

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    public JdbcTemplate(DataSource dataSource) {
        super(dataSource);
    }

    public void update(final String query, Object... args) {
        executeUpdate((conn) -> {
            PreparedStatement ps = conn.prepareStatement(query);
            setParameters(ps, args);
            return ps;
        });
    }

    private void setParameters(PreparedStatement ps, Object[] args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            ps.setObject(i + 1, args[i]);
        }
    }

    public <T> T query(String sql, ResultSetExtractor<T> rse, Object... args) {
        return executeQuery(
            (conn) -> {
                PreparedStatement preparedStatement = conn.prepareStatement(sql);
                setParameters(preparedStatement, args);
                return preparedStatement;
            },
            rse
        );
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper) {
        return executeQuery(
            (conn) -> conn.prepareStatement(sql),
            rowMapper
        );
    }
}
