package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcResourceHandler {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcResourceHandler(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Object handle(String sql, JdbcStrategy jdbcStrategy, Object... objects) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = createPreparedStatement(sql, connection, objects)){
            log.info("query : {}", sql);
            return jdbcStrategy.apply(preparedStatement);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }
    }

    private PreparedStatement createPreparedStatement(String sql, Connection connection, Object... objects)
            throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        for (int i = 0; i < objects.length; i++) {
            preparedStatement.setObject(i+1, objects[i]);
        }
        return preparedStatement;
    }
}
