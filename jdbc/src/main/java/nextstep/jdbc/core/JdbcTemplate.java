package nextstep.jdbc.core;

import nextstep.jdbc.exception.DataAccessException;
import nextstep.jdbc.support.ResultSetExtractor;
import nextstep.jdbc.support.StatementCallback;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcTemplate {

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(final String sql, final Object... params) {
        return execute(sql, stmt -> {
            final PreparedStatement preparedStatement = convertToPreparedStatement(stmt);
            setParameters(preparedStatement, params);
            return preparedStatement.executeUpdate();
        });
    }

    public <T> T query(final String sql, final ResultSetExtractor<T> resultSetExtractor, final Object... params) {
        return execute(sql, stmt -> {
            final PreparedStatement preparedStatement = convertToPreparedStatement(stmt);
            setParameters(preparedStatement, params);
            final ResultSet resultSet = preparedStatement.executeQuery();
            return resultSetExtractor.extract(resultSet);
        });
    }

    public <T> List<T> queryForList(final String sql, final ResultSetExtractor<T> resultSetExtractor, final Object... params) {
        return execute(sql, stmt -> {
            final PreparedStatement preparedStatement = convertToPreparedStatement(stmt);
            setParameters(preparedStatement, params);
            final ResultSet resultSet = preparedStatement.executeQuery();
            final List<T> results = new ArrayList<>();
            while (resultSet.next()) {
                results.add(resultSetExtractor.extract(resultSet));
            }
            return results;
        });
    }

    private PreparedStatement convertToPreparedStatement(final Statement statement) {
        if (!(statement instanceof PreparedStatement)) {
            throw new DataAccessException("PreparedStatement가 아닙니다.");
        }
        return (PreparedStatement) statement;
    }

    private void setParameters(final PreparedStatement preparedStatement, final Object... params) {
        for (int i = 0; i < params.length; i++) {
            ParameterInjector.inject(preparedStatement, i + 1, params[i]);
        }
    }

    private <T> T execute(final String sql, final StatementCallback<T> statementCallback) {
        try (final Connection connection = getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            return statementCallback.doInStatement(preparedStatement);
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
