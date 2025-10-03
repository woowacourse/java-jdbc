package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.interface21.dao.DataAccessException;
import java.util.List;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        final JdbcDataSource jdbcDatasource = new JdbcDataSource();
        jdbcDatasource.setURL("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        jdbcDatasource.setUser("sa");
        jdbcDatasource.setPassword("");

        jdbcTemplate = new JdbcTemplate(jdbcDatasource);
        createDefaultTable(jdbcTemplate);
        insertDefaultValue(jdbcTemplate);
    }

    private void createDefaultTable(final JdbcTemplate jdbcTemplate) {
        final String sql = "create table users (id bigint auto_increment, name varchar(50))";
        jdbcTemplate.update(sql, pstmt -> {});
    }

    private void insertDefaultValue(final JdbcTemplate jdbcTemplate) {
        final String sql = "insert into users (id, name) values (?, ?)";
        jdbcTemplate.update(sql, pstmt -> {
            pstmt.setInt(1, 1);
            pstmt.setString(2, "듀이");
        });
    }

    @AfterEach
    void tearDown() {
        final String sql = "drop table users";
        jdbcTemplate.update(sql, pstmt -> {});
    }

    @Test
    void testInsert() {
        final String sql = "insert into users (id, name) values (?, ?)";
        jdbcTemplate.update(sql, pstmt -> {
            pstmt.setLong(1, 2);
            pstmt.setString(2, "듀2");
        });

        final User user = findUserById(2L);

        assertAll(
                () -> assertThat(user.getId()).isEqualTo(2),
                () -> assertThat(user.getName()).isEqualTo("듀2")
        );
    }

    @Test
    void testUpdate() {
        final String sql = "update users set name = ? where id = ?";
        jdbcTemplate.update(sql, pstmt -> {
            pstmt.setString(1, "듀2");
            pstmt.setLong(2, 1);
        });

        final User user = findUserById(1L);

        assertAll(
                () -> assertThat(user.getId()).isEqualTo(1),
                () -> assertThat(user.getName()).isEqualTo("듀2")
        );
    }

    @Test
    void testQueryForObject() {
        final User user = findUserById(1L);

        assertAll(
                () -> assertThat(user).isNotNull(),
                () -> assertThat(user.getId()).isEqualTo(1),
                () -> assertThat(user.getName()).isEqualTo("듀이")
        );
    }

    private User findUserById(final Long id) {
        return jdbcTemplate.queryForObject("select * from users where id = ?",
                pstmt -> pstmt.setLong(1, id),
                rs -> new User(rs.getLong("id"), rs.getString("name"))
        );
    }

    @Test
    void testQueryForList() {
        final String sql = "select * from users";
        final List<User> users = jdbcTemplate.query(sql,
                rs -> new User(rs.getLong("id"), rs.getString("name"))
        );

        assertThat(users).hasSize(1);
    }

    @Test
    void testException() {
        final String invalidSql = "insert into not_exists_table (id) values (?)";

        assertThatThrownBy(() -> {
            jdbcTemplate.update(invalidSql, pstmt -> pstmt.setLong(1, 1));
        }).isInstanceOf(DataAccessException.class);
    }

    private static class User {
        private final Long id;
        private final String name;

        public User(final Long id, final String name) {
            this.id  = id;
            this.name = name;
        }

        public Long getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }
}
