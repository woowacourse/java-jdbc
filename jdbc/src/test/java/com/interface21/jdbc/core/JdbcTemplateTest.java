package com.interface21.jdbc.core;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


class JdbcTemplateTest {

    private JdbcDataSource dataSource;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = new JdbcDataSource();
        dataSource.setUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
        dataSource.setUser("");
        dataSource.setPassword("");

        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()
        ) {
            statement.execute("""
                    create table users (
                        id bigint auto_increment,
                        account varchar(100) not null,
                        password varchar(100) not null,
                        email varchar(100) not null,
                        primary key(id)
                    );

                    create table user_history (
                        id bigint auto_increment,
                        user_id bigint not null,
                        account varchar(100) not null,
                        password varchar(100) not null,
                        email varchar(100) not null,
                        created_at datetime not null,
                        created_by varchar(100) not null,
                        primary key(id)
                    );
                    """);
        }

        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @AfterEach
    void tearDown() throws SQLException {
        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()
        ) {
            statement.execute("SHUTDOWN");
        }
    }

    @Test
    void insert() {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";
        final int affectedRows = jdbcTemplate.update(sql, "account1", "password1", "email1");

        assertThat(affectedRows).isEqualTo(1);
    }

    @Test
    void update() {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(sql, "account1", "password1", "email1");

        final var sql2 = "update users set account = ?, password = ?, email = ? where id = ?";
        final int affectedRows = jdbcTemplate.update(sql2, "account2", "password1", "email1", 1);

        assertThat(affectedRows).isEqualTo(1);
    }

    @Test
    void queryForObject() {
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(sql, "account3", "password3", "email3");

        final var sql2 = "select id, account, password, email from users where id = ?";
        User user = jdbcTemplate.queryForObject(
                sql2,
                (rs, rowNum) -> new User(
                        rs.getLong("id"),
                        rs.getString("account"),
                        rs.getString("password"),
                        rs.getString("email")
                ),
                1L
        );

        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getAccount()).isEqualTo("account3");
        assertThat(user.getPassword()).isEqualTo("password3");
        assertThat(user.getEmail()).isEqualTo("email3");
    }

    @Test
    void query() {
        final var sql1 = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(sql1, "account4", "password4", "email4");
        final var sql2 = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(sql2, "account5", "password5", "email5");
        final var sql3 = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(sql3, "account6", "password6", "email6");

        final var sql = "select id, account, password, email from users";
        List<User> users = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new User(
                        rs.getLong("id"),
                        rs.getString("account"),
                        rs.getString("password"),
                        rs.getString("email")
                )
        );

        assertThat(users.size()).isEqualTo(3);

        assertThat(users.get(0).getAccount()).isEqualTo("account4");
        assertThat(users.get(0).getPassword()).isEqualTo("password4");
        assertThat(users.get(0).getEmail()).isEqualTo("email4");

        assertThat(users.get(1).getAccount()).isEqualTo("account5");
        assertThat(users.get(1).getPassword()).isEqualTo("password5");
        assertThat(users.get(1).getEmail()).isEqualTo("email5");

        assertThat(users.get(2).getAccount()).isEqualTo("account6");
        assertThat(users.get(2).getPassword()).isEqualTo("password6");
        assertThat(users.get(2).getEmail()).isEqualTo("email6");
    }

    private class User {

        private Long id;
        private final String account;
        private String password;
        private final String email;

        public User(long id, String account, String password, String email) {
            this.id = id;
            this.account = account;
            this.password = password;
            this.email = email;
        }

        public User(String account, String password, String email) {
            this.account = account;
            this.password = password;
            this.email = email;
        }

        public boolean checkPassword(String password) {
            return this.password.equals(password);
        }

        public void changePassword(String password) {
            this.password = password;
        }

        public String getAccount() {
            return account;
        }

        public long getId() {
            return id;
        }

        public String getEmail() {
            return email;
        }

        public String getPassword() {
            return password;
        }

        @Override
        public String toString() {
            return "User{" +
                    "id=" + id +
                    ", account='" + account + '\'' +
                    ", email='" + email + '\'' +
                    ", password='" + password + '\'' +
                    '}';
        }
    }
}
