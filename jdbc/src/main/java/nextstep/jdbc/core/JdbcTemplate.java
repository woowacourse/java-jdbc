package nextstep.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import nextstep.jdbc.exception.DataAccessException;
import nextstep.jdbc.exception.MultipleRowException;
import nextstep.jdbc.exception.ParameterSettingException;
import nextstep.jdbc.support.PreparedStatementExecutor;
import nextstep.jdbc.support.PreparedStatementSetter;
import nextstep.jdbc.support.ResultSetExtractor;
import org.springframework.jdbc.datasource.DataSourceUtils;

public class JdbcTemplate {

    private static final int SINGLE_RESULT_SIZE = 1;

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(final String sql, final Object... params) {
        final PreparedStatementSetter preparedStatementSetter
                = preparedStatement -> setParameters(preparedStatement, params);

        return execute(sql, preparedStatementSetter, PreparedStatement::executeUpdate);
    }

    public <T> T queryForObject(final String sql, final ResultSetExtractor<T> resultSetExtractor,
                                final Object... params) {
        final List<T> results = queryForList(sql, resultSetExtractor, params);
        if (results.isEmpty()) {
            return null;
        }
        if (results.size() > SINGLE_RESULT_SIZE) {
            throw new MultipleRowException();
        }

        return results.iterator()
                .next();
    }

    public <T> List<T> queryForList(final String sql, final ResultSetExtractor<T> resultSetExtractor,
                                    final Object... params) {
        final PreparedStatementSetter preparedStatementSetter
                = preparedStatement -> setParameters(preparedStatement, params);

        final PreparedStatementExecutor<List<T>> preparedStatementExecutor = preparedStatement -> {
            try (final ResultSet resultSet = preparedStatement.executeQuery()) {
                final List<T> results = new ArrayList<>();
                while (resultSet.next()) {
                    results.add(resultSetExtractor.extract(resultSet));
                }
                return results;
            }
        };

        return execute(sql, preparedStatementSetter, preparedStatementExecutor);
    }

    private void setParameters(final PreparedStatement preparedStatement, final Object... params) {
        for (int i = 0; i < params.length; i++) {
            try {
                preparedStatement.setObject(i + 1, params[i]);
            } catch (SQLException e) {
                throw new ParameterSettingException();
            }
        }
    }

    public boolean execute(final String sql) {
        return execute(sql, preparedStatement -> {
        }, PreparedStatement::execute);
    }

    private <T> T execute(final String sql, final PreparedStatementSetter preparedStatementSetter,
                          final PreparedStatementExecutor<T> preparedStatementExecutor) {
        final Connection connection = getConnection();
        try (final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatementSetter.set(preparedStatement);
            return preparedStatementExecutor.execute(preparedStatement);
        } catch (SQLException e) {
            throw new DataAccessException("데이터 접근에 실패하였습니다.", e);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    private Connection getConnection() {
        return DataSourceUtils.getConnection(dataSource);
    }
}
