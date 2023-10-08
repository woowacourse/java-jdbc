package nextstep.jdbc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

public class JdbcTemplateTest {

    private DataSource dataSource;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        dataSource = TestDataSourceConfig.getInstance();
        jdbcTemplate = new JdbcTemplate(dataSource);
        String sql = "CREATE TABLE IF NOT EXISTS users (id BIGINT AUTO_INCREMENT PRIMARY KEY, account VARCHAR(100) NOT NULL)";
        jdbcTemplate.update(sql);
    }

    @Test
    void resultSetIsClosed() throws SQLException {
        //given
        final String sql = "select id, account from users";
        final ResultSet resultSet;
        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            resultSet = preparedStatement.executeQuery();
            assertThat(resultSet.isClosed()).isFalse();
        }
        assertThat(resultSet.isClosed()).isTrue();
    }
}
