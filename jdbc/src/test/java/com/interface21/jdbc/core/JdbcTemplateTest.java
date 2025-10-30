package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.interface21.dao.DataAccessException;
import java.util.List;
import java.util.Optional;
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

        this.jdbcTemplate = new JdbcTemplate(jdbcDatasource);

        createDefaultTable(jdbcTemplate);
        insertDefaultValue(jdbcTemplate);
    }

    private void createDefaultTable(final JdbcTemplate jdbcTemplate) {
        final String sql = "create table users (id bigint auto_increment, name varchar(50))";
        jdbcTemplate.update(sql);
    }

    private void insertDefaultValue(final JdbcTemplate jdbcTemplate) {
        final String sql = "insert into users (id, name) values (?, ?)";
        jdbcTemplate.update(sql, 1, "듀이");
    }

    @AfterEach
    void tearDown() {
        final String sql = "drop table users";
        jdbcTemplate.update(sql);
    }

    @Test
    void testInsert() {
        final String sql = "insert into users (id, name) values (?, ?)";
        final Long returnKey = jdbcTemplate.updateAndReturnKey(sql, 2, "듀2");

        final User user = findUserById(returnKey).get();

        assertAll(
                () -> assertThat(user.getId()).isEqualTo(returnKey),
                () -> assertThat(user.getName()).isEqualTo("듀2")
        );
    }

    @Test
    void testUpdate() {
        final String sql = "update users set name = ? where id = ?";
        jdbcTemplate.update(sql, "듀2", 1);

        final User user = findUserById(1L).get();

        assertAll(
                () -> assertThat(user.getId()).isEqualTo(1),
                () -> assertThat(user.getName()).isEqualTo("듀2")
        );
    }

    @Test
    void testQueryForObject() {
        final User user = findUserById(1L).get();

        assertAll(
                () -> assertThat(user).isNotNull(),
                () -> assertThat(user.getId()).isEqualTo(1),
                () -> assertThat(user.getName()).isEqualTo("듀이")
        );
    }

    @Test
    void testQueryForObject_isEmpty() {
        final Long notExistsId = 999L;
        final Optional<User> user = findUserById(notExistsId);

        assertThat(user).isEmpty();
    }

    private Optional<User> findUserById(final Long id) {
        return jdbcTemplate.queryForObject("select * from users where id = ?",
                rs -> new User(rs.getLong("id"), rs.getString("name")),
                id
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
    void testQueryForList_isEmpty() {
        jdbcTemplate.update("delete from users");

        final String sql = "select * from users";
        final List<User> users = jdbcTemplate.query(sql,
                rs -> new User(rs.getLong("id"), rs.getString("name"))
        );

        assertThat(users).hasSize(0);
    }

    @Test
    void testException_notExistsTable() {
        final String invalidSql = "insert into not_exists_table (id) values (?)";

        assertThatThrownBy(() ->
                jdbcTemplate.update(invalidSql, 1)
        ).isInstanceOf(DataAccessException.class);
    }

    @Test
    void testException_mismatchParameterCount() {
        final String sql = "insert into users (id, name) values (?, ?)";

        assertThatThrownBy(() ->
                jdbcTemplate.update(sql, 1)
        ).isInstanceOf(DataAccessException.class);
    }

    @Test
    void testException_queryForObject_resultIsGreaterThanOne() {
        final String duplicateName = "듀이";
        final String insertSql = "insert into users (id, name) values (?, ?)";
        jdbcTemplate.updateAndReturnKey(insertSql, 2, duplicateName);

        final String selectSql = "select * from users where name = ?";

        assertThatThrownBy(() ->
                jdbcTemplate.queryForObject(
                        selectSql,
                        rs -> new User(rs.getLong("id"), rs.getString("name")),
                        duplicateName
        )).isInstanceOf(DataAccessException.class);
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
