package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DataSourceUtils;

public class JdbcResourceHandler {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcResourceHandler(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Object executeQuery(String sql, JdbcStrategy jdbcStrategy, Object... objects) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (
             PreparedStatement preparedStatement = createPreparedStatement(sql, connection, objects);
             ResultSet resultSet = preparedStatement.executeQuery()){
            log.info("query : {}", sql);
            return jdbcStrategy.apply(resultSet);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    public void execute(String sql, Object... objects) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (
             PreparedStatement preparedStatement = createPreparedStatement(sql, connection, objects)){
            log.info("query : {}", sql);
            preparedStatement.execute();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
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
