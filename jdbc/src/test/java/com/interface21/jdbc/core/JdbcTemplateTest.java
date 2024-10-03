package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.h2.jdbc.JdbcSQLSyntaxErrorException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import support.TestDataSourceConfig;
import support.TestUser;

class JdbcTemplateTest {

    private JdbcTemplate jdbcTemplate;
    private DataSource dataSource;

    @BeforeEach
    void setUp() {
        dataSource = TestDataSourceConfig.getInstance();
        jdbcTemplate = new JdbcTemplate(dataSource);
        createTable();
    }

    @AfterEach
    void tearDown() {
        dropTable();
    }

    @DisplayName("INSERT 쿼리 정상 실행")
    @Test
    void executeUpdate_Insert() {
        // given
        String sql = "INSERT INTO test_user(name, age) values ('John', 20)";

        // when
        int rowCount = jdbcTemplate.executeUpdate(sql);

        // then
        assertThat(rowCount).isOne();
    }

    @DisplayName("INSERT 쿼리 정상 실행: parameter 사용")
    @Test
    void executeUpdate_Insert_UsingParameters() {
        // given
        String sql = "INSERT INTO test_user(name, age) values (?, ?)";

        // when
        int rowCount = jdbcTemplate.executeUpdate(sql, "John", 20);

        // then
        assertThat(rowCount).isOne();
    }

    @DisplayName("UPDATE 쿼리 정상 실행")
    @Test
    void executeUpdate_Update() {
        // given
        jdbcTemplate.executeUpdate("INSERT INTO test_user(name, age) values ('John', 20)");

        // when
        int rowCount = jdbcTemplate.executeUpdate("UPDATE test_user SET name='Change', age=25 WHERE id=1");

        // then
        assertThat(rowCount).isOne();
    }

    @DisplayName("SQL 문법이 틀리면 예외 발생")
    @Test
    void executeUpdate_BadSyntax() {
        assertThatThrownBy(() -> jdbcTemplate.executeUpdate("INSERT INT test_user(name, age) values ('John', 20)"))
                .hasCauseInstanceOf(JdbcSQLSyntaxErrorException.class);
    }

    @DisplayName("올바르지 않은 TYPE으로 INSERT시 예외 발생")
    @Test
    void executeUpdate_IllegalType() {
        assertThatThrownBy(() -> jdbcTemplate.executeUpdate("INSERT INTO test_user(name, age) values ('John, '20')"))
                .hasCauseInstanceOf(JdbcSQLSyntaxErrorException.class);
    }

    @DisplayName("조회 정상 실행: 결과값 0개")
    @Test
    void findAll_NoResults() {
        // given
        jdbcTemplate.executeUpdate("INSERT INTO test_user(name, age) values ('John', 20)");

        // when
        List<TestUser> users = jdbcTemplate.queryForObject(
                "SELECT id, name, age FROM test_user WHERE age=?", TestUser.class, 19);

        // then
        assertThat(users).isEmpty();
    }

    @DisplayName("조회 정상 실행: 결과값 2개")
    @Test
    void findAll_TwoResults() {
        // given
        jdbcTemplate.executeUpdate("INSERT INTO test_user(name, age) values ('John', 20)");
        jdbcTemplate.executeUpdate("INSERT INTO test_user(name, age) values ('Jake', 20)");

        // when
        List<TestUser> users = jdbcTemplate.queryForObject(
                "SELECT id, name, age FROM test_user WHERE age=?", TestUser.class, 20);

        // then
        assertThat(users).containsExactly(
                new TestUser(1L, "John", 20),
                new TestUser(2L, "Jake", 20)
        );
    }

    private void createTable() {
        String sql = """
                CREATE TABLE test_user
                (
                id      INT             NOT NULL AUTO_INCREMENT PRIMARY KEY,
                name    VARCHAR(255)    NOT NULL,
                age     INT             NOT NULL
                );
                """;

        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void dropTable() {
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement("DROP TABLE test_user;")) {
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
