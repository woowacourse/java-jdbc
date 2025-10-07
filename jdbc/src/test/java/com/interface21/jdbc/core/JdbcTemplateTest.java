package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.List;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private static final String DEFAULT_ACCOUNT = "gugu";
    private static final String DEFAULT_PASSWORD = "password";
    private static final String DEFAULT_EMAIL = "gugu@woowahan.com";

    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        final var jdbcDataSource = new JdbcDataSource();
        jdbcDataSource.setUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;");
        jdbcDataSource.setUser("");
        jdbcDataSource.setPassword("");

        jdbcTemplate = new JdbcTemplate(jdbcDataSource);

        initializeDatabase();
    }

    private void initializeDatabase() {
        jdbcTemplate.update("DROP TABLE IF EXISTS users");
        jdbcTemplate.update(
                "CREATE TABLE users (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "account VARCHAR(255) NOT NULL, " +
                "password VARCHAR(255) NOT NULL, " +
                "email VARCHAR(255) NOT NULL" +
                ")"
        );
    }

    @Test
    void update_insert() {
        final var sql = "INSERT INTO users (account, password, email) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, DEFAULT_ACCOUNT, DEFAULT_PASSWORD, DEFAULT_EMAIL);

        final var result = jdbcTemplate.queryForObject(
                "SELECT account FROM users WHERE account = ?",
                (rs, rowNum) -> rs.getString("account"),
                DEFAULT_ACCOUNT
        );

        assertThat(result).isEqualTo(DEFAULT_ACCOUNT);
    }

    @Test
    void update_modify() {
        final var insertSql = "INSERT INTO users (account, password, email) VALUES (?, ?, ?)";
        jdbcTemplate.update(insertSql, DEFAULT_ACCOUNT, DEFAULT_PASSWORD, DEFAULT_EMAIL);

        final var newPassword = "newPassword";
        final var updateSql = "UPDATE users SET password = ? WHERE account = ?";
        jdbcTemplate.update(updateSql, newPassword, DEFAULT_ACCOUNT);

        final var result = jdbcTemplate.queryForObject(
                "SELECT password FROM users WHERE account = ?",
                (rs, rowNum) -> rs.getString(DEFAULT_PASSWORD),
                DEFAULT_ACCOUNT
        );

        assertThat(result).isEqualTo(newPassword);
    }

    @Test
    void queryForObject_found() {
        final var sql = "INSERT INTO users (account, password, email) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, DEFAULT_ACCOUNT, DEFAULT_PASSWORD, DEFAULT_EMAIL);

        final var result = jdbcTemplate.queryForObject(
                "SELECT account, password, email FROM users WHERE account = ?",
                (rs, rowNum) -> new TestUser(
                        rs.getString("account"),
                        rs.getString("password"),
                        rs.getString("email")
                ),
                DEFAULT_ACCOUNT
        );

        assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result.account).isEqualTo(DEFAULT_ACCOUNT),
                () -> assertThat(result.password).isEqualTo(DEFAULT_PASSWORD),
                () -> assertThat(result.email).isEqualTo(DEFAULT_EMAIL)
        );
    }

    @Test
    void queryForObject_notFound() {
        final var result = jdbcTemplate.queryForObject(
                "SELECT account FROM users WHERE account = ?",
                (rs, rowNum) -> rs.getString("account"),
                "nonexistent"
        );

        assertThat(result).isNull();
    }

    @Test
    void query_multipleResults() {
        final var sql = "INSERT INTO users (account, password, email) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, DEFAULT_ACCOUNT, DEFAULT_PASSWORD, DEFAULT_EMAIL);
        jdbcTemplate.update(sql, "pobi", DEFAULT_PASSWORD, "pobi@woowahan.com");
        jdbcTemplate.update(sql, "neo", DEFAULT_PASSWORD, "neo@woowahan.com");

        final List<String> results = jdbcTemplate.query(
                "SELECT account FROM users",
                (rs, rowNum) -> rs.getString("account")
        );

        assertThat(results).hasSize(3)
                .containsExactly(DEFAULT_ACCOUNT, "pobi", "neo");
    }

    @Test
    void query_emptyResult() {
        final List<String> results = jdbcTemplate.query(
                "SELECT account FROM users WHERE account = ?",
                (rs, rowNum) -> rs.getString("account"),
                "nonexistent"
        );

        assertThat(results).isEmpty();
    }

    @Test
    void update_withException() {
        assertThatThrownBy(() -> jdbcTemplate.update("INVALID SQL", DEFAULT_ACCOUNT))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void query_withException() {
        assertThatThrownBy(() ->
                jdbcTemplate.query("INVALID SQL", (rs, rowNum) -> rs.getString("account"), DEFAULT_ACCOUNT)
        ).isInstanceOf(RuntimeException.class);
    }

    private static class TestUser {
        final String account;
        final String password;
        final String email;

        TestUser(String account, String password, String email) {
            this.account = account;
            this.password = password;
            this.email = email;
        }
    }
}
