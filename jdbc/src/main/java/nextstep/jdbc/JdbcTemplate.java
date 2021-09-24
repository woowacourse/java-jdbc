package nextstep.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(String query, Object... args) throws SQLException {
        log.info("JdbcTemplate.update, query: {}", query);
        execute(query, PreparedStatement::executeUpdate, args);
    }

    public <T> List<T> query(String query, RowMapper<T> rowMapper, Object... args) throws SQLException {
        log.info("JdbcTemplate.query, query: {}", query);
        return execute(query, preparedStatement -> {
            ResultSet resultSet = preparedStatement.executeQuery();
            List<T> result = new ArrayList<>();
            while (resultSet.next()) {
                result.add(rowMapper.map(resultSet));
            }
            return result;
        }, args);
    }

    public <T> T queryForObject(String query, RowMapper<T> rowMapper, Object... args) throws SQLException {
        log.info("JdbcTemplate.queryForObject, query: {}", query);
        return execute(query, preparedStatement -> {
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return rowMapper.map(resultSet);
            }
            return null;
        }, args);
    }

    private <T> T execute(String query, PreparedStatementExecutor<T> preparedStatementExecutor, Object... args) throws SQLException {
        Connection connection = dataSource.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        try (connection; preparedStatement) {
            int index = 1;
            for (Object arg : args) {
                preparedStatement.setObject(index++, arg);
            }
            return preparedStatementExecutor.execute(preparedStatement);
        } catch (Exception e) {
            log.debug("JdbcTemplate execution failed: {}", e.getMessage());
            throw new IllegalArgumentException(e.getMessage());
        }
    }
}
