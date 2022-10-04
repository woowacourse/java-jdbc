package nextstep.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import nextstep.jdbc.exception.DataAccessException;
import nextstep.jdbc.support.PreparedStatementCallback;
import nextstep.jdbc.support.ResultSetExtractor;

public class JdbcTemplate {

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(final String sql, final Object... params) {
        return execute(sql, pstmt -> {
            setParameters(pstmt, params);
            return pstmt.executeUpdate();
        });
    }

    public <T> T query(final String sql, final ResultSetExtractor<T> resultSetExtractor, final Object... params) {
        return execute(sql, pstmt -> {
            setParameters(pstmt, params);
            try (final ResultSet resultSet = pstmt.executeQuery()) {
                return resultSetExtractor.extract(resultSet);
            }
        });
    }

    public <T> List<T> queryForList(final String sql, final ResultSetExtractor<T> resultSetExtractor,
                                    final Object... params) {
        return execute(sql, pstmt -> {
            setParameters(pstmt, params);
            try (final ResultSet resultSet = pstmt.executeQuery()) {
                final List<T> results = new ArrayList<>();
                while (resultSet.next()) {
                    results.add(resultSetExtractor.extract(resultSet));
                }
                return results;
            }
        });
    }

    private void setParameters(final PreparedStatement preparedStatement, final Object... params) {
        for (int i = 0; i < params.length; i++) {
            ParameterInjector.inject(preparedStatement, i + 1, params[i]);
        }
    }

    private <T> T execute(final String sql, final PreparedStatementCallback<T> preparedStatementCallback) {
        try (final Connection connection = getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            return preparedStatementCallback.doInStatement(preparedStatement);
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
