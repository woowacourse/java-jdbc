package nextstep.jdbc;

import static org.assertj.core.api.Assertions.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.ConnectionManager;
import org.springframework.jdbc.core.JdbcTemplate;

class JdbcTemplateTest {

    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        TestConnectionManager connectionManager = new TestConnectionManager();
        this.jdbcTemplate = new JdbcTemplate(connectionManager);
        try (Connection conn = connectionManager.getConnection()) {
            conn.setAutoCommit(true);
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("DROP TABLE IF EXISTS users;");
                stmt.execute("CREATE TABLE IF NOT EXISTS users (id INT AUTO_INCREMENT PRIMARY KEY, email VARCHAR(100) NOT NULL)");
                stmt.executeUpdate("INSERT INTO users (email) VALUES ('hkkang@woowahan.com')");
                conn.setAutoCommit(false);
            }
        } catch (SQLException e) {
            throw new AssertionError(e);
        }
    }

    @Test
    @DisplayName("쓰기 작업(생성, 수정, 삭제)을 하는 쿼리를 실행한다.")
    void executeUpdate() {
        try {
            jdbcTemplate.executeUpdate("insert into users (email) values (?)", "doy@gmail.com");
        } catch (Exception exception) {
            Assertions.fail(exception);
        }
    }

    @Test
    @DisplayName("단건 레코드를 조회하는 쿼리를 실행한다.")
    void executeQueryForObject() {
        try {
            User user = jdbcTemplate.executeQueryForObject(
                    "select email from users where id = ?",
                    resultSet -> new User(resultSet.getString(1)), 1L
            );

            assertThat(user).usingRecursiveComparison()
                    .comparingOnlyFields("hkkang@woowahan.com");
        } catch (Exception exception) {
            Assertions.fail(exception);
        }
    }

    @Test
    @DisplayName("다건 레코드를 조회하는 쿼리를 실행한다.")
    void executeQueryForList() {
        try {
            jdbcTemplate.executeUpdate("insert into users (email) values (?)", "doy@gmail.com");

            List<User> users = jdbcTemplate.executeQueryForList(
                    "select email from users",
                    resultSet -> new User(resultSet.getString(1))
            );

            assertThat(users).extracting("email")
                    .containsExactlyInAnyOrder("hkkang@woowahan.com", "doy@gmail.com");
        } catch (Exception exception) {
            Assertions.fail(exception);
        }
    }

    private static class User {

        private final String email;

        public User(final String email) {
            this.email = email;
        }

    }

    private static class TestConnectionManager implements ConnectionManager {

        @Override
        public Connection getConnection() throws CannotGetJdbcConnectionException {
            try {
                final var jdbcDataSource = new JdbcDataSource();
                jdbcDataSource.setUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;");
                return jdbcDataSource.getConnection();
            } catch (SQLException sqlException) {
                throw new AssertionError();
            }
        }
    }
}
