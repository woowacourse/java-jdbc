package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import nextstep.jdbc.exception.JdbcNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T query(final String sql, final RowMapper<T> rowMapper, final Object... arguments) {
        JdbcCallback<T> jdbcCallback = resultSet -> {
            if (!resultSet.next()) {
                throw new JdbcNotFoundException(String.format("조건을 만족하는 행을 찾지 못했습니다.\nsql:(%s)\n", sql));
            }
            return rowMapper.map(resultSet);
        };
        return executeAndThen(sql, jdbcCallback, arguments);
    }

    public <T> List<T> queryAsList(final String sql, final RowMapper<T> rowMapper, final Object... arguments) {
        JdbcCallback<List<T>> jdbcCallback = resultSet -> {
            List<T> results = new ArrayList<>();
            while (resultSet.next()) {
                results.add(rowMapper.map(resultSet));
            }

            return results;
        };
        return executeAndThen(sql, jdbcCallback, arguments);
    }

    private <T> T executeAndThen(final String sql, final JdbcCallback<T> jdbcCallback, final Object... arguments) {
        try (Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            ResultSet resultSet = getResultSet(preparedStatement, arguments)) {
            log.debug("query : {}", sql);
            return jdbcCallback.call(resultSet);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private ResultSet getResultSet(final PreparedStatement preparedStatement, final Object... arguments) throws SQLException {
        for (int argumentIndex = 1; argumentIndex <= arguments.length; argumentIndex++) {
            preparedStatement.setObject(argumentIndex, arguments[argumentIndex - 1]);
            log.debug("binding parameter [{}] as [{}] - [{}]",
                argumentIndex,
                preparedStatement.getParameterMetaData().getParameterTypeName(argumentIndex),
                arguments[argumentIndex - 1]);
        }

        return preparedStatement.executeQuery();
    }
}
