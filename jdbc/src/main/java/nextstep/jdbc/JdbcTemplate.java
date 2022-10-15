package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.sql.DataSource;
import org.springframework.jdbc.datasource.DataSourceUtils;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(final String sql, final Object... args) {
        execute(sql, preparedStatement -> {
            final PreparedStatementSetterImpl statementSetter = new PreparedStatementSetterImpl(args);
            statementSetter.setValues(preparedStatement);
            return preparedStatement.executeUpdate();
        });
    }

    private <T> T execute(String sql, PreparedStatementCallback<T> callback) {
        Connection conn = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement statement = conn.prepareStatement(sql)
        ) {
            log.debug("query : {}", sql);
            return callback.doStatement(statement);
        } catch (SQLException e) {
            log.error("Execute Query Failed: " + e);
            throw new DataAccessException("Query를 성공적으로 실행하지 못했습니다.");
        }
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper,
                                final Object... args) {
        final List<T> results = query(sql, rowMapper, args);
        if (results.size() != 1) {
            throw new DataAccessException("queryForObject는 결괏값이 1개여야 합니다.");
        }
        return results.iterator()
                .next();
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper,
                             final Object... args
    ) {
        return execute(sql, preparedStatement -> {
            final PreparedStatementSetterImpl statementSetter = new PreparedStatementSetterImpl(args);
            statementSetter.setValues(preparedStatement);
            return getResultSetData(preparedStatement, rowMapper);
        });
    }

    private <T> List<T> getResultSetData(final PreparedStatement preparedStatement, final RowMapper<T> rowMapper) {
        try (final ResultSet resultSet = preparedStatement.executeQuery()) {
            final RowMapperResultSetExtractor<T> resultSetExtractor = new RowMapperResultSetExtractor<>(rowMapper);
            return resultSetExtractor.extractData(resultSet);
        } catch (SQLException e) {
            log.error("Execute Query Failed: " + e);
            throw new DataAccessException("Query를 성공적으로 실행하지 못했습니다.");
        }
    }

    public DataSource getDataSource() {
        return dataSource;
    }
}
