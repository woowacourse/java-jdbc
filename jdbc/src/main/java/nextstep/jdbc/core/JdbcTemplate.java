package nextstep.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import nextstep.jdbc.exception.DataAccessException;
import nextstep.jdbc.support.PreparedStatementExecutor;
import nextstep.jdbc.support.PreparedStatementSetter;
import nextstep.jdbc.support.ResultSetExtractor;

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
            throw new DataAccessException("데이터가 1개 이상입니다.");
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
                throw new DataAccessException("파라미터 세팅에 실패하였습니다.");
            }
        }
    }

    private <T> T execute(final String sql, final PreparedStatementSetter preparedStatementSetter,
                          final PreparedStatementExecutor<T> preparedStatementExecutor) {
        try (final Connection connection = getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatementSetter.set(preparedStatement);
            return preparedStatementExecutor.execute(preparedStatement);
        } catch (SQLException e) {
            throw new DataAccessException("데이터 접근에 실패하였습니다.", e);
        }
    }

    private Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new DataAccessException("Connection 점유에 실패하였습니다.", e);
        }
    }
}
