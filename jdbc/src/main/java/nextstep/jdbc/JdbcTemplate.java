package nextstep.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(final String sql, final Object... args) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            setParameters(args, preparedStatement);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Query 에러가 발생했습니다.");
        }
    }

    public <T> List<T> query(final String sql, RowMapper<T> rowMapper, final Object... args) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            setParameters(args, preparedStatement);
            return getResult(preparedStatement, new ResultSetExtractor<>(rowMapper));
        } catch (SQLException e) {
            throw new DataAccessException("Query 에러가 발생했습니다.");
        }
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, Object... args) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            log.debug("query : {}", sql);
            setParameters(args, preparedStatement);
            List<T> result = getResult(preparedStatement, new ResultSetExtractor<>(rowMapper));
            checkResultSizeIsOne(result);
            return result.iterator().next();
        } catch (SQLException e) {
            throw new DataAccessException("Query 에러가 발생했습니다.");
        }
    }

    private <T> void checkResultSizeIsOne(final List<T> result) {
        if (result.isEmpty()) {
            throw new DataAccessException("결과가 없습니다.");
        }
        if (result.size() > 1) {
            throw new DataAccessException("결과가 2개 이상입니다.");
        }
    }

    private <T> List<T> getResult(final PreparedStatement preparedStatement,
                                  final ResultSetExtractor<T> resultSetExtractor) throws SQLException {
        try (final ResultSet resultSet = preparedStatement.executeQuery()) {
            return resultSetExtractor.extractData(resultSet);
        }
    }

    private void setParameters(final Object[] args, final PreparedStatement preparedStatement) throws SQLException {
        int index = 1;
        for (Object arg : args) {
            preparedStatement.setObject(index++, arg);
        }
    }
}
