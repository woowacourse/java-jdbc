package com.interface21.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private static JdbcTemplate jdbcTemplate;

    @BeforeAll
    public static void init() {
        JdbcDataSource jdbcDataSource = new JdbcDataSource();
        jdbcDataSource.setUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;");
        jdbcDataSource.setUser("");
        jdbcDataSource.setPassword("");
        jdbcTemplate = new JdbcTemplate(jdbcDataSource);
    }

    @BeforeEach
    public void setUp() {
        jdbcTemplate.command("create table food (name varchar(255), cost int)");
    }

    @AfterEach
    public void tearDown() {
        jdbcTemplate.command("drop table food");
    }

    @DisplayName("데이터 추가 테스트")
    @Test
    void insert() {
        String insertQuery = "insert into food (name, cost) values (?, ?)";
        String selectQuery = "select * from food where name = ?";
        jdbcTemplate.command(insertQuery, "떡볶이", 8000);

        Food food = jdbcTemplate.queryForObject(this::mapToObject, selectQuery, "떡볶이");

        assertThat(food.cost).isEqualTo(8000);
    }

    @DisplayName("데이터 수정 테스트")
    @Test
    void update() {
        String insertQuery = "insert into food (name, cost) values (?, ?)";
        String updateQuery = "update food set cost = ? where name = ?";
        String selectQuery = "select * from food where name = ?";
        jdbcTemplate.command(insertQuery, "떡볶이", 8000);
        jdbcTemplate.command(updateQuery, 10000, "떡볶이");

        Food food = jdbcTemplate.queryForObject(this::mapToObject, selectQuery, "떡볶이");

        assertThat(food.cost).isEqualTo(10000);
    }

    @DisplayName("데이터 삭제 테스트")
    @Test
    void delete() {
        String insertQuery = "insert into food (name, cost) values (?, ?)";
        String deleteQuery = "delete from food where name = ?";
        String selectQuery = "select * from food where name = ?";
        jdbcTemplate.command(insertQuery, "떡볶이", 8000);
        jdbcTemplate.command(deleteQuery, "떡볶이");

        Food food = jdbcTemplate.queryForObject(this::mapToObject, selectQuery, "떡볶이");

        assertThat(food).isNull();
    }

    @DisplayName("데이터 조회 테스트 - 단건")
    @Test
    void selectSingle() {
        String insertQuery = "insert into food (name, cost) values (?, ?)";
        String selectQuery = "select * from food where name = ?";
        jdbcTemplate.command(insertQuery, "떡볶이", 8000);

        Food food = jdbcTemplate.queryForObject(this::mapToObject, selectQuery, "떡볶이");

        assertThat(food.cost).isEqualTo(8000);
    }

    @DisplayName("데이터 조회 테스트 - 목록")
    @Test
    void selectMulti() {
        String insertQuery = "insert into food (name, cost) values (?, ?)";
        String selectQuery = "select * from food";
        jdbcTemplate.command(insertQuery, "달콤 떡볶이", 8000);
        jdbcTemplate.command(insertQuery, "매콤 떡볶이", 9000);

        List<Food> foods = jdbcTemplate.queryForList(this::mapToObject, selectQuery);

        assertThat(foods).hasSize(2);
        assertThat(foods).extracting("name").contains("달콤 떡볶이", "매콤 떡볶이");
    }

    private Food mapToObject(ResultSet rs) {
        try {
            return new Food(
                    rs.getString("name"),
                    rs.getInt("cost")
            );
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    static class Food {

        private String name;
        private int cost;

        public Food(String name, int cost) {
            this.name = name;
            this.cost = cost;
        }
    }
}
