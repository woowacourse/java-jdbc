package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import nextstep.jdbc.exception.QueryException;

public class JdbcTemplate {

    private final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(String query, Object... querySubject) {
        return execute(PreparedStatement::executeUpdate, query, querySubject);
    }

    public <T> List<T> queryObjects(String query, RowMapper<T> rowMapper) {
        return execute(preparedStatement -> translateObject(preparedStatement, rowMapper), query);
    }

    public <T> Optional<T> queryObject(String query, RowMapper<T> rowMapper,
        Object... querySubject) {
        return execute(preparedStatement -> executeQueryWithRowMapper(preparedStatement, rowMapper),
            query, querySubject);
    }

    private <T> T execute(PrepareStatementAction<T> prepareStatementAction,
        String query, Object... querySubject) {
        try (
            Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(query)
        ) {
            PrepareStatementSetter.setValue(preparedStatement, querySubject);
            return prepareStatementAction.execute(preparedStatement);
        } catch (SQLException e) {
            throw new QueryException(e.getMessage());
        }
    }

    private <T> Optional<T> executeQueryWithRowMapper(PreparedStatement preparedStatement,
        RowMapper<T> rowMapper) throws SQLException {
        try (ResultSet resultSet = preparedStatement.executeQuery()) {
            return Optional.ofNullable(ObjectMapper.mappedObject(resultSet, rowMapper));
        }
    }

    private <T> List<T> translateObject(PreparedStatement preparedStatement, RowMapper<T> rowMapper)
        throws SQLException {
        try (ResultSet resultSet = preparedStatement.executeQuery()) {
            List<T> objects = new LinkedList<>();
            while (resultSet.next()) {
                T t = ObjectMapper.mappedObject(resultSet, rowMapper);
                objects.add(t);
            }
            return objects;
        }
    }
}
