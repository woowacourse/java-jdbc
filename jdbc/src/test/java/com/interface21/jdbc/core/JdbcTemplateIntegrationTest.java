package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JdbcTemplateIntegrationTest {

    private static final String INSERT_SQL = "insert into members (id, name) values (?, ?)";
    private static final RowMapper<Member> MEMBER_ROW_MAPPER = resultSet -> new Member(
            resultSet.getLong("id"), resultSet.getString("name"));

    private Connection databaseConnection;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws SQLException {
        final var dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:" + UUID.randomUUID());
        databaseConnection = dataSource.getConnection();
        try (final var statement = databaseConnection.createStatement()) {
            statement.execute("create table members (id bigint primary key, name varchar(50))");
        }
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @AfterEach
    void tearDown() throws SQLException {
        databaseConnection.close();
    }

    @Test
    void updateAndQueryWithVariableArguments() {
        jdbcTemplate.update(INSERT_SQL, 1L, "kaki");
        jdbcTemplate.update(INSERT_SQL, 2L, "gugu");
        jdbcTemplate.update("update members set name = ? where id = ?", "changed", 1L);

        final var members = jdbcTemplate.query("select id, name from members where id >= ? order by id",
                MEMBER_ROW_MAPPER, 1L);

        assertThat(members).containsExactly(new Member(1L, "changed"), new Member(2L, "gugu"));
    }

    @Test
    void updateAndQueryWithPreparedStatementSetter() {
        jdbcTemplate.update(INSERT_SQL, statement -> {
            statement.setLong(1, 1L);
            statement.setString(2, "kaki");
        });

        final var members = jdbcTemplate.query("select id, name from members where id = ?",
                MEMBER_ROW_MAPPER, statement -> statement.setLong(1, 1L));

        assertThat(members).containsExactly(new Member(1L, "kaki"));
    }

    @Test
    void queryForObjectWithPreparedStatementSetter() {
        jdbcTemplate.update(INSERT_SQL, 1L, "kaki");

        final var member = jdbcTemplate.queryForObject("select id, name from members where id = ?",
                MEMBER_ROW_MAPPER, statement -> statement.setLong(1, 1L));

        assertThat(member).isEqualTo(new Member(1L, "kaki"));
    }

    @Test
    void queryReturnsEmptyListWhenNoRowsMatch() {
        final var members = jdbcTemplate.query("select id, name from members", MEMBER_ROW_MAPPER);

        assertThat(members).isEmpty();
    }

    @Test
    void queryForObjectReturnsNullWhenNoRowsMatch() {
        final var member = jdbcTemplate.queryForObject("select id, name from members where id = ?",
                MEMBER_ROW_MAPPER, 1L);

        assertThat(member).isNull();
    }

    @Test
    void queryForObjectSupportsDifferentResultTypes() {
        jdbcTemplate.update(INSERT_SQL, 1L, "kaki");

        final Long count = jdbcTemplate.queryForObject("select count(*) as total from members",
                resultSet -> resultSet.getLong("total"));
        final String name = jdbcTemplate.queryForObject("select name from members where id = ?",
                resultSet -> resultSet.getString("name"), 1L);

        assertThat(count).isEqualTo(1L);
        assertThat(name).isEqualTo("kaki");
    }

    @Test
    void constraintViolationPreservesCauseAndAllowsNextOperation() {
        jdbcTemplate.update(INSERT_SQL, 1L, "kaki");

        assertThatThrownBy(() -> jdbcTemplate.update(INSERT_SQL, 1L, "duplicate"))
                .isInstanceOf(DataAccessException.class)
                .hasCauseInstanceOf(SQLException.class);

        jdbcTemplate.update(INSERT_SQL, 2L, "gugu");
        final var members = jdbcTemplate.query("select id, name from members order by id", MEMBER_ROW_MAPPER);
        assertThat(members).containsExactly(new Member(1L, "kaki"), new Member(2L, "gugu"));
    }

    private record Member(long id, String name) {
    }
}
