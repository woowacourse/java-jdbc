package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import org.h2.jdbcx.JdbcConnectionPool;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JdbcTemplateTest {

    private JdbcConnectionPool dataSource;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setup() throws SQLException {
        dataSource = JdbcConnectionPool.create("jdbc:h2:mem:" + UUID.randomUUID(), "", "");
        dataSource.setMaxConnections(1);
        dataSource.setLoginTimeout(1);
        jdbcTemplate = new JdbcTemplate(dataSource);
        try (var connection = dataSource.getConnection();
             var statement = connection.createStatement()) {
            statement.execute("create table users (id bigint primary key, account varchar(100), email varchar(100))");
            statement.execute("insert into users values (1, 'first', 'first@example.com'), (2, 'second', 'second@example.com')");
        }
    }

    @AfterEach
    void tearDown() {
        try {
            assertThat(dataSource.getActiveConnections()).isZero();
        } finally {
            dataSource.dispose();
        }
    }

    @Test
    void insertBindsArguments() {
        assertThat(jdbcTemplate.update("insert into users values (?, ?, ?)",
                3L, "third", "third@example.com")).isEqualTo(1);

        final String account = jdbcTemplate.queryForObject("select account from users where id = ?",
                (rs, rowNum) -> rs.getString("account"), 3L);
        assertThat(account).isEqualTo("third");
    }

    @Test
    void updateBindsNullAndId() {
        assertThat(jdbcTemplate.update("update users set email = ? where id = ?", null, 1L)).isEqualTo(1);

        final var emails = jdbcTemplate.query("select email from users order by id",
                (rs, rowNum) -> rs.getString("email"));
        assertThat(emails).containsExactly(null, "second@example.com");
    }

    @Test
    void queryMapsEveryRow() {
        assertThat(jdbcTemplate.query("select account from users order by id",
                (rs, rowNum) -> rowNum + ":" + rs.getString("account")))
                .containsExactly("0:first", "1:second");
    }

    @Test
    void queryReturnsEmptyListForNoRows() {
        assertThat(jdbcTemplate.query("select account from users where id = ?",
                (rs, rowNum) -> rs.getString("account"), 999L)).isEmpty();
    }

    @Test
    void queryForObjectReturnsNullForNoRows() {
        final String account = jdbcTemplate.queryForObject("select account from users where id = ?",
                (rs, rowNum) -> rs.getString("account"), 999L);
        assertThat(account).isNull();
    }

    @Test
    void queryForObjectRejectsMultipleRows() {
        assertThatThrownBy(() -> jdbcTemplate.queryForObject("select account from users",
                (rs, rowNum) -> rs.getString("account"))).isInstanceOf(DataAccessException.class);
    }

    @Test
    void accountIsBoundAsAParameter() {
        assertThat(jdbcTemplate.query("select account from users where account = ?",
                (rs, rowNum) -> rs.getString("account"), "first' OR '1'='1")).isEmpty();
    }

    @Test
    void mappingFailurePreservesCauseAndReturnsConnection() {
        final SQLException failure = new SQLException("mapping failed");

        assertThatThrownBy(() -> jdbcTemplate.query("select account from users", (rs, rowNum) -> {
            throw failure;
        })).isInstanceOf(DataAccessException.class).hasCause(failure);

        assertConnectionReturnedAndReusable();
    }

    @Test
    void invalidSqlPreservesCauseAndReturnsConnection() {
        assertThatThrownBy(() -> jdbcTemplate.update("invalid SQL"))
                .isInstanceOf(DataAccessException.class).hasCauseInstanceOf(SQLException.class);

        assertConnectionReturnedAndReusable();
    }

    @Test
    void constraintViolationReturnsConnection() {
        assertThatThrownBy(() -> jdbcTemplate.update("insert into users values (?, ?, ?)",
                1L, "duplicate", "duplicate@example.com"))
                .isInstanceOf(DataAccessException.class).hasCauseInstanceOf(SQLException.class);

        assertConnectionReturnedAndReusable();
    }

    @Test
    void bindingFailureReturnsConnection() {
        assertThatThrownBy(() -> jdbcTemplate.update("update users set email = ?", "email", 1L))
                .isInstanceOf(DataAccessException.class).hasCauseInstanceOf(SQLException.class);
        assertConnectionReturnedAndReusable();
    }

    @Test
    void callerOwnsConnectionAndCanRollback() throws SQLException {
        try (var connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            jdbcTemplate.update(connection, "update users set email = ? where id = ?", "changed", 1L);
            final String email = jdbcTemplate.queryForObject(connection,
                    "select email from users where id = ?", (rs, rowNum) -> rs.getString(1), 1L);
            assertThat(email).isEqualTo("changed");
            assertThat(connection.isClosed()).isFalse();
            connection.rollback();
        }
        final String email = jdbcTemplate.queryForObject("select email from users where id = ?",
                (rs, rowNum) -> rs.getString(1), 1L);
        assertThat(email).isEqualTo("first@example.com");
    }

    @Test
    void uncheckedMappingFailureReturnsConnection() {
        final IllegalStateException failure = new IllegalStateException("mapping failed");

        assertThatThrownBy(() -> jdbcTemplate.query("select account from users", (rs, rowNum) -> {
            throw failure;
        })).isSameAs(failure);

        assertConnectionReturnedAndReusable();
    }

    @Test
    void sqlFailureDoesNotCloseCallerConnection() throws SQLException {
        try (var connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            assertThatThrownBy(() -> jdbcTemplate.update(connection, "invalid SQL"))
                    .isInstanceOf(DataAccessException.class);
            assertThat(connection.isClosed()).isFalse();
            connection.rollback();
        }
        assertConnectionReturnedAndReusable();
    }

    private void assertConnectionReturnedAndReusable() {
        assertThat(dataSource.getActiveConnections()).isZero();
        final Long count = jdbcTemplate.queryForObject("select count(*) from users",
                (rs, rowNum) -> rs.getLong(1));
        assertThat(count).isEqualTo(2L);
    }
}
