package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private final DataSource dataSource;

    private static final Logger LOGGER = LoggerFactory.getLogger(JdbcTemplate.class);

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void update(final String sql, final Object... args) {
        LOGGER.info("sql query: {}", sql);
        try (Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
        ) {
            updateQuery(preparedStatement, args);
        } catch (SQLException exception) {
            LOGGER.debug("exception occurred while sql execute");
            LOGGER.debug("exception message: {}", exception.getMessage());
        }
    }

    private void updateQuery(final PreparedStatement preparedStatement, final Object[] args) throws SQLException {
        for (int i = 0; i < args.length; ++i) {
            preparedStatement.setObject(i + 1, args[i]);
        }
        preparedStatement.executeUpdate();
    }
}
