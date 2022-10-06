package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.sql.DataSource;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(final String sql, final PreparedStatementSetter preparedStatementSetter) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(sql)
        ) {
            preparedStatementSetter.setValues(preparedStatement);
            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            log.error("Execute Query Failed: " + e);
            throw new DataAccessException("Query를 성공적으로 실행하지 못했습니다.");
        }
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper,
                             final PreparedStatementSetter preparedStatementSetter
    ) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(sql)
        ) {
            preparedStatementSetter.setValues(preparedStatement);
            return getResultSetData(preparedStatement, rowMapper);
        } catch (SQLException e) {
            log.error("Execute Query Failed: " + e);
            throw new DataAccessException("Query를 성공적으로 실행하지 못했습니다.");
        }
    }

    private <T> List<T> getResultSetData(final PreparedStatement preparedStatement,
                                  final RowMapper<T> rowMapper) throws SQLException {
        try (final ResultSet resultSet = preparedStatement.executeQuery()) {
            final RowMapperResultSetExtractor<T> resultSetExtractor = new RowMapperResultSetExtractor<>(rowMapper);
            return resultSetExtractor.extractData(resultSet);
        } catch (SQLException e) {
            log.error("Execute Query Failed: " + e);
            throw new DataAccessException("Query를 성공적으로 실행하지 못했습니다.");
        }
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper,
                                final PreparedStatementSetter preparedStatementSetter) {
        final List<T> results = query(sql, rowMapper, preparedStatementSetter);
        if (results.isEmpty()) {
            throw new DataAccessException("Query를 성공적으로 실행하지 못했습니다.");
        }
        if (results.size() > 1) {
            throw new DataAccessException("Query를 성공적으로 실행하지 못했습니다.");
        }
        return results.iterator()
                .next();
    }
}
