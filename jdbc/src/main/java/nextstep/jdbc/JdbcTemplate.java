package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

public class JdbcTemplate {

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(String sql, Object... args) {
        execute(sql, PreparedStatement::executeUpdate, args);
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        List<T> results = query(sql, rowMapper, args);
        return DataAccessUtils.nullableSingleResult(results);
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) {
        return execute(sql, preparedStatement -> getResult(preparedStatement, rowMapper), args);
    }

    private <T> T execute(String sql, PreparedStatementCallback<T> action, Object... args) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            setParameters(args, preparedStatement);
            return action.doInPreparedStatement(preparedStatement);
        } catch (SQLException e) {
            throw new DataAccessException();
        }
    }

    private void setParameters(Object[] args, PreparedStatement preparedStatement) throws SQLException {
        int index = 1;
        for (Object arg : args) {
            preparedStatement.setObject(index++, arg);
        }
    }

    private <T> List<T> getResult(PreparedStatement preparedStatement, RowMapper<T> rowMapper) throws SQLException {
        try (ResultSet resultSet = preparedStatement.executeQuery()) {
            List<T> result = new ArrayList<>();
            int rowNum = 0;
            while (resultSet.next()) {
                result.add(rowMapper.mapRow(resultSet, rowNum++));
            }
            return result;
        }
    }
}
