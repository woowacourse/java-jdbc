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

public class JdbcTemplate<T> {

    private final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int execute(String query, Object... querySubject) {
        try (
            Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(query)
        ) {
            setValue(preparedStatement, querySubject);
            preparedStatement.executeUpdate();
            return 1;
        } catch (SQLException e) {
            throw new QueryException(e.getMessage());
        }
    }

    public List<T> queryObjects(String query, RowMapper<T> rowMapper) {
        try (
            Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(query)
        ) {
            return translateObject(preparedStatement, rowMapper);
        } catch (SQLException e) {
            throw new QueryException(e.getMessage());
        }
    }

    public Optional<T> queryObjectWithCondition(String query, RowMapper<T> rowMapper,
        Object... querySubject) {
        try (
            Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(query)
        ) {
            setValue(preparedStatement, querySubject);
            ResultSet resultSet = preparedStatement.executeQuery();
            return Optional.ofNullable(rowMapper.rowMappedObject(resultSet));
        } catch (SQLException e) {
            throw new QueryException(e.getMessage());
        }
    }

    private List<T> translateObject(PreparedStatement preparedStatement, RowMapper<T> rowMapper)
        throws SQLException {
        ResultSet resultSet = preparedStatement.executeQuery();
        List<T> domains = new LinkedList<>();
        while (resultSet.next()) {
            T t = rowMapper.rowMappedObject(resultSet);
            domains.add(t);
        }
        return domains;
    }

    private void setValue(PreparedStatement preparedStatement, Object... querySubject)
        throws SQLException {
        for (int i = 0; i < querySubject.length; i++) {
            preparedStatement.setObject(i + 1, querySubject[i]);
        }
    }
}
