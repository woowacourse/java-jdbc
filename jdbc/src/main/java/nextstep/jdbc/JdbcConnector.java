package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;

public class JdbcConnector {

    private DataSource dataSource;

    public JdbcConnector(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T execute(Execution<T> execution) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(execution.getSql())) {
            return execution.execute(statement);
        } catch (SQLException e) {
//            log.info(e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
