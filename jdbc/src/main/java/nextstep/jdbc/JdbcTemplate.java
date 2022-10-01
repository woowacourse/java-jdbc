package nextstep.jdbc;

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

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;
    private final JdbcConnector connector;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
        this.connector = new JdbcConnector(dataSource);
    }

    public void update(String sql, Object[] objects) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < objects.length; i++) {
                statement.setObject(i + 1, objects[i]);
            }

            statement.executeUpdate();
        } catch (SQLException e) {
            log.info(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public <T> List<T> query(String sql, RowMapper<T> rowMapper) {
        return connector.execute(new ListExecution<>(sql, rowMapper));
    }

    public <T> T queryForObject(String sql, Object[] arguments, RowMapper<T> rowMapper) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < arguments.length; i++) {
                statement.setObject(i + 1, arguments[i]);
            }
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return rowMapper.rowMap(resultSet, resultSet.getRow());
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
