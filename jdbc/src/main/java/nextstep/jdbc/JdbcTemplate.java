package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import nextstep.jdbc.exception.ImpossibleSQLExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DataSourceUtils;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final Connector connector;
    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.connector = new JdbcConnector(dataSource);
        this.dataSource = dataSource;
    }

    public int update(final String sql, final Object... parameters) {
        return execute(sql, new UpdateExecutor(), parameters);
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... parameters) {
        return forObject(execute(sql, new FindExecutor<>(rowMapper), parameters));
    }

    public <T> List<T> queryForList(final String sql, final RowMapper<T> rowMapper, final Object... parameters) {
        return execute(sql, new FindExecutor<>(rowMapper), parameters);
    }

    private <T> T forObject(final List<T> results) {
        return DataAccessUtils.getSingleResult(results);
    }


    private <T> T execute(final String sql, final QueryExecutor<T> queryExecutor, final Object... parameters) {
        log.debug("query : {}", sql);
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            PreparedStatementStarter preparedStatementStarter = new SimplePreparedStatementStarter(preparedStatement);
            preparedStatementStarter.setParameters(parameters);

            return queryExecutor.executePreparedStatement(preparedStatementStarter);
        } catch (SQLException e) {
            throw new ImpossibleSQLExecutionException();
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }
}
