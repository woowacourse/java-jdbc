package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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

    public int execute(final String sql, final KeyHolder<?> keyHolder, final Object... params) {
        final var creator = PreparedStatementCreator.from(keyHolder, params);
        final var executor = PreparedStatementExecutor.executeUpdateWithKeyHolder(keyHolder);
        return executeQuery(sql, creator, executor);
    }

    public int execute(final String sql, final Object... params) {
        final var creator = PreparedStatementCreator.from(params);
        final PreparedStatementExecutor<Integer> executor = PreparedStatement::executeUpdate;
        return executeQuery(sql, creator, executor);
    }

    public <T> List<T> queryForList(final String sql, final RowMapper<T> rowMapper, final Object... params) {
        final var creator = PreparedStatementCreator.from(params);
        final var executor = PreparedStatementExecutor.mapToListExecutor(rowMapper);
        return executeQuery(sql, creator, executor);
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... params) {
        final var creator = PreparedStatementCreator.from(params);
        final var executor = PreparedStatementExecutor.mapToObjectExecutor(rowMapper);

        return executeQuery(sql, creator, executor);
    }

    private <T> T executeQuery(
            final String sql, final PreparedStatementCreator creator, final PreparedStatementExecutor<T> executor
    ) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement pstmt = creator.create(connection, sql)) {
            log.debug("query : {}", sql);
            return executor.execute(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }
}
