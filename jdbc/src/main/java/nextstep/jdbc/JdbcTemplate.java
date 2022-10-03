package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(final String sql, final PreparedStatementSetter preparedStatementSetter) {
        return execute(sql, getUpdateCallback(preparedStatementSetter));
    }

    public int update(final String sql, final Object... args) {
        return update(sql, statement -> setParameters(statement, args));
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper, final Object... args) {
        return execute(sql, getSingleObjectQueryCallback(rowMapper, args));
    }

    private PreparedStatementCallback<Integer> getUpdateCallback(
            final PreparedStatementSetter preparedStatementSetter) {
        return statement -> {
            preparedStatementSetter.setValues(statement);
            return statement.executeUpdate();
        };
    }

    private <T> PreparedStatementCallback<T> getSingleObjectQueryCallback(final RowMapper<T> rowMapper,
                                                                          final Object[] args) {
        return statement -> {
            setParameters(statement, args);
            final ResultSet resultSet = statement.executeQuery();
            return getSingleObject(rowMapper, resultSet);
        };
    }

    private <T> T getSingleObject(final RowMapper<T> rowMapper, final ResultSet resultSet) throws SQLException {
        if (!resultSet.next()) {
            throw new DataAccessException("해당하는 데이터가 없습니다.");
        }
        return rowMapper.mapRow(resultSet);
    }

    private void setParameters(final PreparedStatement statement, final Object[] args) throws SQLException {
        int index = 1;
        for (Object arg : args) {
            statement.setObject(index, arg);
            index++;
        }
    }

    private <T> T execute(final String sql, final PreparedStatementCallback<T> callback) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            return callback.doInPreparedStatement(statement);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e);
        }
    }
}
