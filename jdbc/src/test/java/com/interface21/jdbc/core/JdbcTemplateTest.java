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
}
