package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.interface21.dao.DataAccessException;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private JdbcDataSource dataSource;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = new JdbcDataSource();
        dataSource.setUrl("jdbc:h2:mem:jdbc_template_"
                + UUID.randomUUID().toString().replace("-", "") + ";DB_CLOSE_DELAY=-1");
        dataSource.setUser("sa");
        jdbcTemplate = new JdbcTemplate(dataSource);

        try (var connection = dataSource.getConnection();
             var statement = connection.createStatement()) {
            statement.execute("create table users ("
                    + "id bigint auto_increment primary key, "
                    + "account varchar(100) not null, "
                    + "email varchar(100) not null)");
            statement.executeUpdate("insert into users (account, email) values "
                    + "('gugu', 'gugu@example.com'), ('pobi', 'pobi@example.com')");
        }
    }

    @Test
    void updateBindsParameters() throws SQLException {
        jdbcTemplate.update("update users set account = ?, email = ? where id = ?",
                new Object[]{"changed", "changed@example.com", 1L});

        try (var connection = dataSource.getConnection();
             var statement = connection.createStatement();
             var resultSet = statement.executeQuery("select account, email from users where id = 1")) {
            assertThat(resultSet.next()).isTrue();
            assertThat(resultSet.getString("account")).isEqualTo("changed");
            assertThat(resultSet.getString("email")).isEqualTo("changed@example.com");
        }
    }

    @Test
    void updateWrapsSqlExceptionAndPreservesCause() {
        assertThatThrownBy(() -> jdbcTemplate.update(
                "update users set account = ? where", "changed"))
                .isInstanceOf(DataAccessException.class)
                .hasCauseInstanceOf(SQLException.class);
    }

    @Test
    void updateWithConnectionParticipatesInTheCallersTransaction() throws SQLException {
        try (var connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);

            jdbcTemplate.update(connection, "update users set account = ? where id = ?", "changed", 1L);

            assertThat(connection.isClosed()).isFalse();
            final var uncommittedAccount = jdbcTemplate.queryForObject(connection,
                    "select account from users where id = ?", rs -> rs.getString("account"), 1L);
            assertThat(uncommittedAccount).isEqualTo("changed");
            final var committedAccount = jdbcTemplate.queryForObject(
                    "select account from users where id = ?", rs -> rs.getString("account"), 1L);
            assertThat(committedAccount).isEqualTo("gugu");

            connection.rollback();
        }

        final var account = jdbcTemplate.queryForObject(
                "select account from users where id = ?", rs -> rs.getString("account"), 1L);
        assertThat(account).isEqualTo("gugu");
    }

    @Test
    void queryWithConnectionSeesUncommittedChangesAndLeavesConnectionOpen() throws SQLException {
        try (var connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            jdbcTemplate.update(connection, "update users set account = ? where id = ?", "changed", 1L);

            final var accounts = jdbcTemplate.query(connection,
                    "select account from users where id > ? order by id", rs -> rs.getString("account"), 0L);

            assertThat(accounts).containsExactly("changed", "pobi");
            assertThat(connection.isClosed()).isFalse();
            connection.rollback();
        }
    }

    @Test
    void updateWithConnectionLeavesConnectionOpenOnSqlFailure() throws SQLException {
        try (var connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);

            assertThatThrownBy(() -> jdbcTemplate.update(connection,
                    "update users set account = ? where", "changed"))
                    .isInstanceOf(DataAccessException.class)
                    .hasCauseInstanceOf(SQLException.class);

            assertThat(connection.isClosed()).isFalse();
            connection.rollback();
        }
    }

    @Test
    void queryWithoutParametersMapsEveryRow() {
        List<String> accounts = jdbcTemplate.query(
                "select account from users order by id", rs -> rs.getString("account"));

        assertThat(accounts).containsExactly("gugu", "pobi");
    }

    @Test
    void queryWithParametersMapsMatchingRows() {
        List<String> accounts = jdbcTemplate.query(
                "select account from users where id > ?", rs -> rs.getString("account"), 1L);

        assertThat(accounts).containsExactly("pobi");
    }

    @Test
    void queryWithParametersReturnsAnEmptyListWhenNoRowsMatch() {
        List<String> accounts = jdbcTemplate.query(
                "select account from users where id > ?", rs -> rs.getString("account"), 2L);

        assertThat(accounts).isEmpty();
    }

    @Test
    void queryWrapsSqlExceptionAndPreservesCause() {
        assertThatThrownBy(() -> jdbcTemplate.query(
                "select account from users where", rs -> rs.getString("account")))
                .isInstanceOf(DataAccessException.class)
                .hasCauseInstanceOf(SQLException.class);
    }

    @Test
    void queryWrapsRowMapperSqlExceptionAndPreservesOriginalCause() {
        SQLException cause = new SQLException("Could not map account");
        RowMapper<String> rowMapper = rs -> {
            throw cause;
        };

        assertThatThrownBy(() -> jdbcTemplate.query("select account from users", rowMapper))
                .isInstanceOf(DataAccessException.class)
                .cause()
                .isSameAs(cause);
    }

    @Test
    void queryForObjectReturnsTheSingleRow() {
        String account = jdbcTemplate.queryForObject(
                "select account from users where id = ?", rs -> rs.getString("account"), 1L);

        assertThat(account).isEqualTo("gugu");
    }

    @Test
    void queryForObjectRejectsZeroRows() {
        assertThatThrownBy(() -> jdbcTemplate.queryForObject(
                "select account from users where id = ?", rs -> rs.getString("account"), 99L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("조회 결과를 찾을 수 없습니다.");
    }

    @Test
    void queryForObjectRejectsMultipleRows() {
        assertThatThrownBy(() -> jdbcTemplate.queryForObject(
                "select account from users", rs -> rs.getString("account")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("조회 결과가 2개 이상입니다.");
    }
}
