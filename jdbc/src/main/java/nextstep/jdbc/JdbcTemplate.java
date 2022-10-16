package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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

    public void update(final String sql, final Object... parameters) {
        execute(sql, PreparedStatement::executeUpdate, parameters);
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... parameters) {
        List<T> results = query(sql, rowMapper, parameters);
        SingleResultChecker.checkSingleResult(Collections.singletonList(results));
        return results.get(0);
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... parameters) {
        return execute(sql, ps -> result(ps, re -> extractResult(re, rowMapper)), parameters);
    }

    private <T> T execute(final String sql, final ExecuteQuery<T> query, final Object... parameters) {
        var connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            PreparedStatementSetter.setParameter(statement, parameters);
            return query.executeQuery(statement);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private <T> List<T> result(final PreparedStatement preparedStatement, final ResultExtractor<T> resultExtractor) {
        try (ResultSet resultSet = preparedStatement.executeQuery()) {
            return resultExtractor.extractResult(resultSet);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
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
}
