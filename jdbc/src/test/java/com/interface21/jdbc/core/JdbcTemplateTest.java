package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
    void queryWithoutParametersMapsEveryRow() {
        List<String> accounts = jdbcTemplate.query(
                "select account from users order by id", rs -> rs.getString("account"));

        assertThat(accounts).containsExactly("gugu", "pobi");
    }

    @Test
    void queryWithParametersMapsMatchingRows() {
        List<String> accounts = jdbcTemplate.query(
                "select account from users where id > ?", new Object[]{1L},
                rs -> rs.getString("account"));

        assertThat(accounts).containsExactly("pobi");
    }

    @Test
    void queryWithParametersReturnsAnEmptyListWhenNoRowsMatch() {
        List<String> accounts = jdbcTemplate.query(
                "select account from users where id > ?", new Object[]{2L},
                rs -> rs.getString("account"));

        assertThat(accounts).isEmpty();
    }

    @Test
    void queryForObjectReturnsTheSingleRow() {
        String account = jdbcTemplate.queryForObject(
                "select account from users where id = ?", new Object[]{1L},
                rs -> rs.getString("account"));

        assertThat(account).isEqualTo("gugu");
    }

    @Test
    void queryForObjectRejectsZeroOrMultipleRows() {
        RowMapper<String> mapper = rs -> rs.getString("account");

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(
                "select account from users where id = ?", new Object[]{99L}, mapper))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> jdbcTemplate.queryForObject(
                "select account from users", new Object[]{}, mapper))
                .isInstanceOf(IllegalStateException.class);
    }
}
