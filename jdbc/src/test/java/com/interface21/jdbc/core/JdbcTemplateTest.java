package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.h2.jdbc.JdbcSQLSyntaxErrorException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import support.TestDataSourceConfig;

class JdbcTemplateTest {

    private JdbcTemplate jdbcTemplate;
    private DataSource dataSource;

    @BeforeEach
    void setUp() {
        dataSource = TestDataSourceConfig.getInstance();
        jdbcTemplate = new JdbcTemplate(dataSource);
        createTable();
    }

    @DisplayName("INSERT 쿼리 정상 실행")
    @Test
    void executeUpdate_Insert() {
        // given
        String sql = "INSERT INTO test_table(name, age) values ('John', 20)";

        // when
        int rowCount = jdbcTemplate.executeUpdate(sql);

        // then
        assertThat(rowCount).isOne();
    }

    @DisplayName("UPDATE 쿼리 정상 실행")
    @Test
    void executeUpdate_Update() {
        // given
        jdbcTemplate.executeUpdate("INSERT INTO test_table(name, age) values ('John', 20)");

        // when
        int rowCount = jdbcTemplate.executeUpdate("UPDATE test_table SET name='Change', age=25 WHERE id=1");

        // then
        assertThat(rowCount).isOne();
    }

    @DisplayName("SQL 문법이 틀리면 예외 발생")
    @Test
    void executeUpdate_BadSyntax() {
        assertThatThrownBy(() -> jdbcTemplate.executeUpdate("INSERT INT test_table(name, age) values ('John', 20)"))
                .hasCauseInstanceOf(JdbcSQLSyntaxErrorException.class);
    }

    @DisplayName("올바르지 않은 TYPE으로 INSERT시 예외 발생")
    @Test
    void executeUpdate_IllegalType() {
        assertThatThrownBy(() -> jdbcTemplate.executeUpdate("INSERT INTO test_table(name, age) values ('John, '20')"))
                .hasCauseInstanceOf(JdbcSQLSyntaxErrorException.class);
    }

    private void createTable() {
        String sql = """
                CREATE TABLE test_table
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
}
