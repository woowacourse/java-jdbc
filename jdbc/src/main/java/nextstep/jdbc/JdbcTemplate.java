package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import nextstep.jdbc.exception.ImpossibleSQLExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import org.springframework.jdbc.datasource.DataSourceUtils;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(final String sql, final Object... parameters) {
        return execute(sql, ps -> ps.executeUpdate(), parameters);
    }

    public <T> T queryForObject(final String sql, RowMapper<T> rowMapper, final Object... parameters) {
        log.debug("query : {}", sql);
        return forObject(execute(sql, ps -> mapResultSet(ps, rowMapper), parameters));
    }

    public <T> List<T> queryForList(final String sql, RowMapper<T> rowMapper, final Object... parameters) {
        log.debug("query : {}", sql);
        return execute(sql, ps -> mapResultSet(ps, rowMapper), parameters);
    }

    private <T> List<T> mapResultSet(PreparedStatement preparedStatement, RowMapper<T> rowMapper) {
        ResultSet resultSet;
        try {
            resultSet = preparedStatement.executeQuery();
            ResultSetExtractor<List<T>> rse = new RowMapperResultSetExtractor<>(rowMapper, 1);
            return rse.extractData(resultSet);
        } catch (SQLException e) {
            throw new ImpossibleSQLExecutionException();
        }
    }

    private <T> T forObject(final List<T> ts) {
        return DataAccessUtils.nullableSingleResult(ts);
    }

    private <T> T execute(final String sql, QueryExecutor<T> queryExecutor, final Object... parameters) {
        try (Connection conn = DataSourceUtils.getConnection(dataSource);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            for (int i = 0; i < parameters.length; i++) {
                ps.setObject(i + 1, parameters[i]);
            }
            return queryExecutor.executePreparedStatement(ps);
        }
        catch (SQLException e) {
            throw new ImpossibleSQLExecutionException();
        }
    }
}
