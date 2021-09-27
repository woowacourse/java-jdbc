package nextstep.jdbc;

import nextstep.jdbc.exception.DataAccessException;
import nextstep.jdbc.exception.EmptyResultDataAccessException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcTemplate {

    private DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        setDataSource(dataSource);
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        return execute(getArgumentPreparedCreator(sql, args), preparedStatement -> {
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return rowMapper.mapRow(resultSet, 0);
            }

            throw new EmptyResultDataAccessException();
        });
    }

    public <T> List<T> queryForList(String sql, RowMapper<T> rowMapper, Object... args) {
        return execute(getArgumentPreparedCreator(sql, args), preparedStatement -> {
            ResultSet resultSet = preparedStatement.executeQuery();
            List<T> result = new ArrayList<>();

            for (int i = 0; resultSet.next(); i++) {
                result.add(rowMapper.mapRow(resultSet, i));
            }
            return result;
        });
    }

    public void update(String sql, Object... args) {
        execute(getArgumentPreparedCreator(sql, args), PreparedStatement::executeUpdate);
    }

    private <T> T execute(PreparedStatementCreator preparedStatementCreator, PreparedStatementCallback<T> action) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = preparedStatementCreator.createPreparedStatement(connection)) {
            return action.doInPreparedStatement(preparedStatement);
        } catch (SQLException sqlException) {
            throw new DataAccessException("execute exception");
        }
    }


    private PreparedStatementCreator getArgumentPreparedCreator(String sql, Object... args) {
        return connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            for (int i = 1; i <= args.length; i++) {
                preparedStatement.setObject(i, args[i - 1]);
            }
            return preparedStatement;
        };
    }
}
