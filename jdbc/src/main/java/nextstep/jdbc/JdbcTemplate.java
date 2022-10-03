package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(final StatementStrategy statementStrategy) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = statementStrategy.makePreparedStatement(connection)) {
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    public DataSource getDataSource() {
        return dataSource;
    }
}
