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
                result.add(rowMapper.map(resultSet, resultSet.getRow()));
            }
            return result;
        }
    }

    public <T> T queryForObject(String query, RowMapper<T> rowMapper, Object... args) throws SQLException {
        Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(query);

        try (connection; statement) {
            int index = 1;
            for (Object arg : args) {
                statement.setObject(index++, arg);
            }

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return rowMapper.map(resultSet, resultSet.getRow());
            }
        }
        throw new SQLException();
    }
}
