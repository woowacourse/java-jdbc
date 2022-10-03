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

    public int update(final String sql, final Object... args) {
        try (final Connection connection = getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            for (int i = 0; i < args.length; i++) {
                preparedStatement.setObject(i + 1, args[i]);
            }

            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("해당 sql문을 실행할 수 없습니다.", e);
        }
    }

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}
