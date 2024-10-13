package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private static JdbcTemplate jdbcTemplate;
    private static Statement statement;

    @BeforeAll
    static void beforeAll() throws SQLException {
        String sql = """
                create table if not exists users (
                    id bigint auto_increment,
                    account varchar(100) not null,
                    password varchar(100) not null,
                    email varchar(100) not null,
                    primary key(id)
                )""";

        JdbcDataSource jdbcDataSource = createJdbcDataSource();
        Connection connection = jdbcDataSource.getConnection();
        statement = connection.createStatement();
        statement.execute(sql);
        jdbcTemplate = new JdbcTemplate(jdbcDataSource);
    }

    private static JdbcDataSource createJdbcDataSource() {
        final var jdbcDataSource = new JdbcDataSource();
        jdbcDataSource.setUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;");
        jdbcDataSource.setUser("");
        jdbcDataSource.setPassword("");
        return jdbcDataSource;
    }

    @BeforeEach
    void setUp() throws SQLException {
        String sql = "delete from users";
        statement.execute(sql);
    }

    @DisplayName("INSERT, UPDATE, DELETE 같은 쿼리를 실행할 수 있다.")
    @Test
    void update() throws SQLException {
        String sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(sql, "admin", "1234", "admin@admin.com");

        ResultSet resultSet = statement.executeQuery("select count(*) from users");
        long count = 0L;
        if (resultSet.next()) {
            count = resultSet.getLong(1);
        }

        assertThat(count).isEqualTo(1L);
    }

    @DisplayName("엔티티 리스트를 찾을 수 있다.")
    @Test
    void query() throws SQLException {
        statement.executeUpdate("""
                insert into users (account, password, email) 
                values 
                    ('account1', 'password1', 'email1'),
                    ('account2', 'password2', 'email2'),
                    ('account3', 'password3', 'email3')
                """);

        String sql = "select * from users";
        List<User> users = jdbcTemplate.query(sql, new BeanPropertyRowMapper<User>(User.class));

        assertThat(users).hasSize(3)
                .extracting("account", "password", "email")
                .containsExactly(
                        tuple("account1", "password1", "email1"),
                        tuple("account2", "password2", "email2"),
                        tuple("account3", "password3", "email3")
                );
    }

    @DisplayName("엔티티 단건을 찾을 수 있다.")
    @Test
    void queryForObject() throws SQLException {
        statement.executeUpdate("""
                insert into users (account, password, email) 
                values 
                    ('account1', 'password1', 'email1'),
                    ('account2', 'password2', 'email2'),
                    ('account3', 'password3', 'email3')
                """);

        String sql = "select * from users where account = ?";
        Optional<User> user = jdbcTemplate.queryForObject(
                sql, new BeanPropertyRowMapper<User>(User.class), "account1");

        assertThat(user).isNotEmpty()
                .get()
                .extracting("account", "password", "email")
                .containsExactly("account1", "password1", "email1");
    }

    @DisplayName("엔티티가 2건 이상이면 오류가 난다.")
    @Test
    void queryForObjectOver() throws SQLException {
        statement.executeUpdate("""
                insert into users (account, password, email) 
                values 
                    ('account1', 'password1', 'email1'),
                    ('account1', 'password2', 'email2'),
                    ('account3', 'password3', 'email3')
                """);

        String sql = "select * from users where account = ?";

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(
                sql, new BeanPropertyRowMapper<User>(User.class), "account1"))
                .isInstanceOf(SQLExecuteException.class)
                .hasMessage("조회된 레코드가 2건 이상입니다.");
    }

    @DisplayName("엔티티가 0건 이면 비어있다.")
    @Test
    void queryForObjectEmpty() throws SQLException {
        statement.executeUpdate("""
                insert into users (account, password, email) 
                values 
                    ('account1', 'password1', 'email1'),
                    ('account2', 'password2', 'email2'),
                    ('account3', 'password3', 'email3')
                """);

        String sql = "select * from users where account = ?";
        Optional<User> user = jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<User>(User.class), "account0");

        assertThat(user).isEmpty();
    }

    private static class User {
        Long id;
        String account;
        String password;
        String email;
    }
}
