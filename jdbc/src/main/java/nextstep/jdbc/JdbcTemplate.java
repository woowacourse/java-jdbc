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
        Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(query);
        try (connection; statement) {
            int index = 1;
            for (Object arg : args) {
                statement.setObject(index++, arg);
            }
            statement.executeUpdate();
        }
    }

    public <T> List<T> query(String query, RowMapper<T> rowMapper, Object... args) throws SQLException {
        Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(query);
        try (connection; statement) {
            int index = 1;
            for (Object arg : args) {
                statement.setObject(index++, arg);
            }

            ResultSet resultSet = statement.executeQuery();
            List<T> result = new ArrayList<>();
            if (resultSet.next()) {
                result.add(rowMapper.map(resultSet));
            }
            return result;
        }
    }

    public <T> T queryForObject(String query, RowMapper<T> rowMapper, Object... args) throws SQLException {
        return execute(query, preparedStatement -> {
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return rowMapper.map(resultSet);
            }
            return null;
        }, args);
    }

    public <T> T execute(String query, PreparedStatementExecutor<T> preparedStatementExecutor, Object... args) throws SQLException {
        Connection connection = dataSource.getConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        try (connection; preparedStatement) {
            int index = 1;
            for (Object arg : args) {
                preparedStatement.setObject(index++, arg);
            }
            return preparedStatementExecutor.execute(preparedStatement);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new IllegalArgumentException(e.getMessage());
        }
    }
}
