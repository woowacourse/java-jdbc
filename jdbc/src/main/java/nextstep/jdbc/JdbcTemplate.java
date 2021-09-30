package nextstep.jdbc;

import exception.DataAccessException;
import exception.IncorrectDataSizeException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    interface QueryResult<T> {

        T getResult(PreparedStatement preparedStatement) throws SQLException;
    }

    public int update(String sql, Object... args) {
        log(sql);
        return execute(sql, PreparedStatement::executeUpdate, args);
    }

    private <T> T execute(String sql, QueryResult<T> queryResult, Object... args) {
        try (
            Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
        ) {
            if (args.length != 0) {
                setValues(preparedStatement, args);
            }
            return queryResult.getResult(preparedStatement);
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private void setValues(PreparedStatement preparedStatement, Object[] args) throws SQLException {
        for (int row = 0; row < args.length; row++) {
            preparedStatement.setObject(row + 1, args[row]);
        }
    }

    public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) {
        log(sql);

        List<T> results = queryForList(sql, rowMapper, args);
        if (results.size() != 1) {
            throw new IncorrectDataSizeException(1, results.size());
        }
        return results.get(0);
    }

    public <T> List<T> queryForList(String sql, RowMapper<T> rowMapper, Object... args) {
        log(sql);

        return execute(
            sql,
            preparedStatement -> {
                try (ResultSet resultSet = executeQuery(preparedStatement, args)) {
                    return mapRows(rowMapper, resultSet);
                } catch (SQLException e) {
                    throw new DataAccessException(e);
                }
            }
        );
    }

    private ResultSet executeQuery(PreparedStatement preparedStatement, Object[] args) throws SQLException {
        setValues(preparedStatement, args);
        return preparedStatement.executeQuery();
    }

    private <T> List<T> mapRows(RowMapper<T> rowMapper, ResultSet resultSet) throws SQLException {
        List<T> results = new ArrayList<>();
        int rowNum = 0;
        while (resultSet.next()) {
            results.add(rowMapper.mapRow(resultSet, rowNum++));
        }
        return results;
    }

    private void log(String sql) {
        log.info("query: {}", sql);
    }
}
