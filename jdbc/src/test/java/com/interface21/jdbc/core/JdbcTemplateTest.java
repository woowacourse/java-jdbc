package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import org.h2.jdbcx.JdbcConnectionPool;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JdbcTemplateTest {

    private JdbcConnectionPool dataSource;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = JdbcConnectionPool.create("jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1", "", "");
        dataSource.setMaxConnections(1);
        jdbcTemplate = new JdbcTemplate(dataSource);
        try (final var connection = dataSource.getConnection();
             final var statement = connection.createStatement()) {
            statement.execute("create table users (id bigint primary key, account varchar(100) not null, email varchar(100))");
        }
    }

    @AfterEach
    void tearDown() throws SQLException {
        try (final var connection = dataSource.getConnection();
             final var statement = connection.createStatement()) {
            statement.execute("drop all objects");
        } finally {
            dataSource.dispose();
        }
    }

    @Test
    void updateBindsArgumentsAndReturnsAffectedRows() throws SQLException {
        final var account = "gugu' OR '1'='1";

        final var affectedRows = jdbcTemplate.update(
                "insert into users (id, account, email) values (?, ?, ?)", 1L, account, "gugu@example.com");

        assertThat(affectedRows).isEqualTo(1);
        try (final var connection = dataSource.getConnection();
             final var statement = connection.createStatement();
             final var resultSet = statement.executeQuery("select id, account, email from users")) {
            assertThat(resultSet.next()).isTrue();
            assertThat(resultSet.getLong("id")).isEqualTo(1L);
            assertThat(resultSet.getString("account")).isEqualTo(account);
            assertThat(resultSet.getString("email")).isEqualTo("gugu@example.com");
            assertThat(resultSet.next()).isFalse();
        }
        assertThat(dataSource.getActiveConnections()).isZero();
    }

    @Test
    void updateSupportsSqlNull() throws SQLException {
        jdbcTemplate.update("insert into users (id, account, email) values (?, ?, ?)", 1L, "gugu", null);

        try (final var connection = dataSource.getConnection();
             final var statement = connection.createStatement();
             final var resultSet = statement.executeQuery("select email from users where id = 1")) {
            assertThat(resultSet.next()).isTrue();
            assertThat(resultSet.getString("email")).isNull();
        }
    }

    @Test
    void updateSupportsModifyingAndDeletingRows() {
        jdbcTemplate.update("insert into users (id, account) values (?, ?)", 1L, "gugu");

        assertThat(jdbcTemplate.update("update users set account = ? where id = ?", "changed", 1L)).isEqualTo(1);
        assertThat(jdbcTemplate.update("update users set account = ? where id = ?", "missing", 2L)).isZero();
        assertThat(jdbcTemplate.update("delete from users")).isEqualTo(1);
        assertThat(dataSource.getActiveConnections()).isZero();
    }

    @Test
    void updateTranslatesPreparationFailureAndReleasesConnection() {
        assertThatThrownBy(() -> jdbcTemplate.update("insert into missing_table values (?)", 1L))
                .isInstanceOf(DataAccessException.class)
                .hasCauseInstanceOf(SQLException.class);

        assertThat(dataSource.getActiveConnections()).isZero();
    }

    @Test
    void updateTranslatesExecutionFailureAndReleasesConnection() {
        assertThatThrownBy(() -> jdbcTemplate.update("insert into users (id, account) values (?, ?)", 1L, null))
                .isInstanceOf(DataAccessException.class)
                .hasCauseInstanceOf(SQLException.class);

        assertThat(dataSource.getActiveConnections()).isZero();
        assertThat(jdbcTemplate.update("insert into users (id, account) values (?, ?)", 1L, "gugu")).isEqualTo(1);
    }

    @Test
    void queryMapsEveryRowWithItsRowNumber() {
        jdbcTemplate.update("insert into users (id, account) values (?, ?), (?, ?)", 1L, "gugu", 2L, "brown");

        final var accounts = jdbcTemplate.query("select account from users where id >= ? order by id",
                (rs, rowNum) -> rowNum + ":" + rs.getString("account"), 1L);

        assertThat(accounts).containsExactly("0:gugu", "1:brown");
        assertThat(dataSource.getActiveConnections()).isZero();
    }

    @Test
    void queryReturnsEmptyListWhenNoRowsMatch() {
        final var accounts = jdbcTemplate.query("select account from users", (rs, rowNum) -> rs.getString("account"));

        assertThat(accounts).isEmpty();
    }

    @Test
    void queryTreatsArgumentsAsValues() {
        final var account = "gugu' OR '1'='1";
        jdbcTemplate.update("insert into users (id, account) values (?, ?), (?, ?)", 1L, account, 2L, "brown");

        final var accounts = jdbcTemplate.query("select account from users where account = ?",
                (rs, rowNum) -> rs.getString("account"), account);

        assertThat(accounts).containsExactly(account);
    }

    @Test
    void queryForObjectReturnsMappedRow() {
        jdbcTemplate.update("insert into users (id, account) values (?, ?)", 1L, "gugu");

        final var account = jdbcTemplate.queryForObject("select account from users where id = ?",
                (rs, rowNum) -> rs.getString("account"), 1L);

        assertThat(account).isEqualTo("gugu");
    }

    @Test
    void queryForObjectReturnsNullWhenNoRowsMatch() {
        final var account = jdbcTemplate.queryForObject("select account from users where id = ?",
                (rs, rowNum) -> rs.getString("account"), 1L);

        assertThat(account).isNull();
    }

    @Test
    void queryForObjectRejectsMultipleRows() {
        jdbcTemplate.update("insert into users (id, account) values (?, ?), (?, ?)", 1L, "gugu", 2L, "brown");

        assertThatThrownBy(() -> jdbcTemplate.queryForObject("select account from users",
                (rs, rowNum) -> rs.getString("account")))
                .isInstanceOf(DataAccessException.class);
        assertThat(dataSource.getActiveConnections()).isZero();
    }

    @Test
    void queryClosesResultSetAndStatement() throws SQLException {
        jdbcTemplate.update("insert into users (id, account) values (?, ?)", 1L, "gugu");
        final var resultSet = new AtomicReference<ResultSet>();
        final var statement = new AtomicReference<Statement>();

        jdbcTemplate.query("select account from users", (rs, rowNum) -> {
            resultSet.set(rs);
            statement.set(rs.getStatement());
            return rs.getString("account");
        });

        assertThat(resultSet.get().isClosed()).isTrue();
        assertThat(statement.get().isClosed()).isTrue();
        assertThat(dataSource.getActiveConnections()).isZero();
    }

    @Test
    void queryTranslatesSqlExceptionFromMapperAndClosesResources() throws SQLException {
        jdbcTemplate.update("insert into users (id, account) values (?, ?)", 1L, "gugu");
        final var failure = new SQLException("mapping failed");
        final var resultSet = new AtomicReference<ResultSet>();
        final var statement = new AtomicReference<Statement>();

        assertThatThrownBy(() -> jdbcTemplate.query("select account from users", (rs, rowNum) -> {
            resultSet.set(rs);
            statement.set(rs.getStatement());
            throw failure;
        })).isInstanceOf(DataAccessException.class).hasCause(failure);

        assertThat(resultSet.get().isClosed()).isTrue();
        assertThat(statement.get().isClosed()).isTrue();
        assertThat(dataSource.getActiveConnections()).isZero();
    }

    @Test
    void queryPreservesRuntimeExceptionFromMapperAndReleasesConnection() {
        jdbcTemplate.update("insert into users (id, account) values (?, ?)", 1L, "gugu");
        final var failure = new IllegalArgumentException("mapping failed");

        assertThatThrownBy(() -> jdbcTemplate.query("select account from users", (rs, rowNum) -> {
            throw failure;
        })).isSameAs(failure);

        assertThat(dataSource.getActiveConnections()).isZero();
    }

    @Test
    void queryTranslatesInvalidSqlAndReleasesConnection() {
        assertThatThrownBy(() -> jdbcTemplate.query("select * from missing_table", (rs, rowNum) -> rs.getLong("id")))
                .isInstanceOf(DataAccessException.class)
                .hasCauseInstanceOf(SQLException.class);

        assertThat(dataSource.getActiveConnections()).isZero();
    }
}
