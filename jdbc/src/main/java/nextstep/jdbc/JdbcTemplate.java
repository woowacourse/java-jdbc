package nextstep.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

public class JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

    private final DataSource dataSource;

    public JdbcTemplate(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int update(String sql, Object... args) {
        return execute(PreparedStatement::executeUpdate, sql, args);
    }

    private <T> T execute(PreparedStatementCallback<T> preparedStatementCallback, String sql, Object... args) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement statement = createStatement(connection, sql, args)) {
            return preparedStatementCallback.call(statement);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e.getMessage());
        }
    }

    private PreparedStatement createStatement(final Connection connection, final String sql, final Object[] args)
            throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(sql);

        int argumentCount = args.length;
        for (int i = 0; i < argumentCount; i++) {
            preparedStatement.setObject(i + 1, args[i]);
        }

        return preparedStatement;
    }
}
