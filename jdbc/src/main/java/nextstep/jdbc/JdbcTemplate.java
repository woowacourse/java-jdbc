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

    public int update(final String sql, final Object... args) {
        return connect(PreparedStatement::executeUpdate, sql, args);
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return connect((PreparedStatement preparedStatement) -> executeQueryForObject(rowMapper, preparedStatement),
                sql, args);
    }

    private <T> T executeQueryForObject(final RowMapper<T> rowMapper, final PreparedStatement preparedStatement)
            throws SQLException {
        List<T> queryResults = executeQuery(rowMapper, preparedStatement);
        if (queryResults.size() == 0) {
            throw new DataAccessException("결과가 존재하지 않습니다.");
        }
        if (queryResults.size() > 1) {
            throw new DataAccessException("결과가 2개 이상입니다.");
        }
        return queryResults.get(0);
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return connect((PreparedStatement preparedStatement) -> executeQuery(rowMapper, preparedStatement), sql, args);
    }

    private <T> T connect(final QueryExecutor<T> executor, final String sql, final Object... args) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            setArgsToPreparedStatement(preparedStatement, args);
            return executor.execute(preparedStatement);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }

    private void setArgsToPreparedStatement(final PreparedStatement preparedStatement, final Object[] args)
            throws SQLException {
        for (int idx = 0; idx < args.length; idx++) {
            preparedStatement.setObject(idx + 1, args[idx]);
        }
    }

    private <T> List<T> executeQuery(final RowMapper<T> rowMapper, final PreparedStatement preparedStatement)
            throws SQLException {
        try (ResultSet resultSet = preparedStatement.executeQuery()) {
            return getQueryResults(rowMapper, resultSet);
        }
    }

    private <T> List<T> getQueryResults(final RowMapper<T> rowMapper, final ResultSet resultSet) throws SQLException {
        List<T> result = new ArrayList<>();
        while (resultSet.next()) {
            result.add(rowMapper.mapRow(resultSet));
        }
        return result;
    }

    public DataSource getDataSource() {
        return dataSource;
    }
}
