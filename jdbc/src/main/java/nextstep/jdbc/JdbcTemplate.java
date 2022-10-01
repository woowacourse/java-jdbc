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

    public void update(final String sql, Object... args) {
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            for (int idx = 0; idx < args.length; idx++) {
                preparedStatement.setString(idx + 1, (String) args[idx]);
            }
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            log.error("Execute Query Failed: " + e);
            throw new RuntimeException("Query를 성공적으로 실행하지 못했습니다.");
        }
    }
}
