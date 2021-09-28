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

    public int update(String sql, Object... args) {
        log(sql);

        return executeUpdate(sql, preparedStatementSetter(args));
    }

    private int executeUpdate(String sql, PreparedStatementSetter preparedStatementSetter) {
        try (
            Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
        ) {
            if (preparedStatementSetter != null) {
                preparedStatementSetter.setValues(preparedStatement);
            }
            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e);
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

        return executeForList(
            sql,
            preparedStatementSetter(args),
            resultSet -> {
                List<T> results = new ArrayList<>();
                int rowNum = 0;
                while (resultSet.next()) {
                    results.add(rowMapper.mapRow(resultSet, rowNum++));
                }
                return results;
            }
        );
    }

    private <T> List<T> executeForList(String sql,
                                       PreparedStatementSetter preparedStatementSetter,
                                       ResultSetExtractor<List<T>> resultSetExtractor
    ) {
        try (
            Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
        ) {
            return resultSetExtractor.extractData(
                executeQuery(preparedStatement, preparedStatementSetter)
            );
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private ResultSet executeQuery(PreparedStatement preparedStatement,
                                   PreparedStatementSetter preparedStatementSetter
    ) throws SQLException {
        if (preparedStatementSetter != null) {
            preparedStatementSetter.setValues(preparedStatement);
        }
        return preparedStatement.executeQuery();
    }

    private PreparedStatementSetter preparedStatementSetter(Object[] args) {
        return preparedStatement -> {
            for (int row = 0; row < args.length; row++) {
                preparedStatement.setObject(row + 1, args[row]);
            }
        };
    }

    private void log(String sql) {
        log.info("query: {}", sql);
    }
}
