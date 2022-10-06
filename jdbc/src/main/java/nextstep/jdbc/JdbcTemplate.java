package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

public class JdbcTemplate {

    private final JdbcResourceHandler jdbcResourceHandler;

    public JdbcTemplate(final DataSource dataSource) {
        this.jdbcResourceHandler = new JdbcResourceHandler(dataSource);
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper) {
        Object result = jdbcResourceHandler.handle(
                sql, (preparedStatement -> extractResult(preparedStatement, rowMapper))
        );

        return (List<T>) result;
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... parameters) {
        Object result = jdbcResourceHandler.handle(
                sql, preparedStatement -> extractResult(preparedStatement, rowMapper), parameters
        );

        List<T> result1 = (List<T>) result;
        return result1.stream()
                .findAny()
                .orElseThrow(() -> new DataAccessException("not found object of query: " + sql));
    }

    private <T> List<T> extractResult(PreparedStatement preparedStatement, RowMapper<T> rowMapper)
            throws SQLException {
        List<T> objects = new ArrayList<>();
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            T object = rowMapper.mapRow(resultSet);
            objects.add(object);
        }
        resultSet.close();
        return objects;
    }

    public void updateQuery(String sql) {
        jdbcResourceHandler.handle(sql, (PreparedStatement::execute));
    }

    public void updateQuery(String sql, Object... parameters) {
        jdbcResourceHandler.handle(sql, PreparedStatement::execute, parameters);
    }
}
