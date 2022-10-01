package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import nextstep.jdbc.execution.ListExecution;
import nextstep.jdbc.execution.ObjectExecution;
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
        return connector.execute(new ObjectExecution<>(sql, arguments, rowMapper));
    }
}
