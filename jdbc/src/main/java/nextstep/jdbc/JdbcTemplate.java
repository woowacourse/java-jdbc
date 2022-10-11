package nextstep.jdbc;

import static nextstep.jdbc.PreparedStatementSetter.setParameter;

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
        execute(sql, PreparedStatement::executeUpdate, parameters);
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... parameters) {
        List<T> results = query(sql, rowMapper, parameters);
        if (results.size() != 1) {
            throw new IllegalStateException();
        }
        return results.get(0);
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... parameters) {
        return (List<T>) execute(sql, ps -> {
            ResultSet resultSet = ps.executeQuery();
            return extractResult(resultSet, rowMapper);
        }, parameters);
    }

    private <T> List<T> extractResult(final ResultSet results, final RowMapper<T> rowMapper) {
        try {
            List<T> result = new ArrayList<>();
            if (results.next()) {
                result.add(rowMapper.mapRow(results));
            }
            return result;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private Object execute(final String sql, final ExecuteQuery<Object> query, final Object... parameters) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            setParameter(statement, parameters);
            return query.executeQuery(statement);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }
}
