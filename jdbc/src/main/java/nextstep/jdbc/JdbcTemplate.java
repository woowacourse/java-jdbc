package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
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

    public void update(final String sql, final Object... args) {
        execute(sql, pstmt -> setParameters(pstmt, args).executeUpdate());
    }

    private <T> T execute(String sql, PreparedStatementCallback<T> preparedStatementCallback) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            return preparedStatementCallback.doPreparedStatement(preparedStatement);
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    private PreparedStatement setParameters(final PreparedStatement preparedStatement, final Object... args)
            throws SQLException {

        for (int i = 0; i < args.length; i++) {
            preparedStatement.setObject(i + 1, args[i]);
        }
        return preparedStatement;
    }

}
