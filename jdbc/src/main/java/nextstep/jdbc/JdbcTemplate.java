package nextstep.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);
    private static final int PREPARED_STATEMENT_START_INDEX = 1;

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private <T> T connect(final String sql,
                          final ExecuteStrategy<T> strategy,
                          final Object... args) {
        try (
            final var connection = dataSource.getConnection();
            final var preparedStatement = connection.prepareStatement(sql)) {

            for (int i = PREPARED_STATEMENT_START_INDEX; i < args.length; i++) {
                preparedStatement.setObject(i, args[i - 1]);
            }
            return strategy.execute(preparedStatement);
        } catch (SQLException e) {
            log.error("Not Data Access : {}", e.getMessage());
            throw new DataAccessException(e);
        }
    }

    interface ExecuteStrategy<T> {
        T execute(final PreparedStatement pstmt) throws SQLException;
    }
}
