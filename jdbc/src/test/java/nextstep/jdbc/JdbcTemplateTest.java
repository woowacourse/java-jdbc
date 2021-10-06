package nextstep.jdbc;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JdbcTemplateTest {

    private static JdbcTemplate jdbcTemplate;

    private final RowMapper<Food> foodRowMapper = rs -> new Food(
            rs.getLong("id"),
            rs.getString("name"),
            rs.getInt("price"));

    @BeforeAll
    static void setUp() {
        final DataSource dataSource = TestDataSource.getInstance();
        jdbcTemplate= new JdbcTemplate(dataSource);

        jdbcTemplate.update(
                "create table food (\n" +
                "    id bigint auto_increment,\n" +
                "    name varchar(100) not null,\n" +
                "    price int not null,\n" +
                "    primary key(id)\n" +
                ");");
    }

    @DisplayName("쿼리를 실행 후, 몇 개의 Row가 변경되었는지 확인할 수 있다.")
    @Test
    void update() {
        String query1 = "insert into food (name, price) values (?, ?)";
        int updatedRows = jdbcTemplate.update(query1, "pizza", 20000);
        assertThat(updatedRows).isOne();

        String query2 = "insert into food (name, price) values (?, ?)";
        updatedRows = jdbcTemplate.update(query2, "chicken", 15000);
        assertThat(updatedRows).isOne();

        String query3 = "update food set name = (?)";
        updatedRows = jdbcTemplate.update(query3, "changeAll");
        assertThat(updatedRows).isEqualTo(2);
    }

    @DisplayName("쿼리를 실행하여 조건에 알맞는 객체를 반환받을 수 있다.")
    @Test
    void queryForObject() {
        // given
        String insertQuery = "insert into food (name, price) values (?, ?)";
        jdbcTemplate.update(insertQuery, "salmon", 15000);
        jdbcTemplate.update(insertQuery, "tuna", 15000);

        // when
        String searchQuery = "select * from food where name = (?)";
        final Food queriedSalmon = jdbcTemplate.queryForObject(searchQuery, foodRowMapper, "salmon");

        // then
        assertThat(queriedSalmon.name).isEqualTo("salmon");
        assertThat(queriedSalmon.price).isEqualTo(15000);
    }

    @DisplayName("쿼리를 실행하여 조건에 알맞는 객체들을 List로 반환받을 수 있다.")
    @Test
    void query() {
        // given
        String insertQuery = "insert into food (name, price) values (?, ?)";
        jdbcTemplate.update(insertQuery, "pasta", 130000);
        jdbcTemplate.update(insertQuery, "sandwich", 100500);
        jdbcTemplate.update(insertQuery, "steak", 125000);

        // when
        String searchQuery = "select * from food where price > 100000";
        final List<Food> queriedFoods = jdbcTemplate.query(searchQuery, foodRowMapper);

        // then
        assertThat(queriedFoods).hasSize(3);
    }

    static class Food {
        Long id;
        String name;
        Integer price;

        public Food(Long id, String name, Integer price) {
            this.id = id;
            this.name = name;
            this.price = price;
        }
    }
}
