package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private static final RowMapper<Car> CAR_ROW_MAPPER = rs -> new Car(rs.getLong(1),
        rs.getString(2), rs.getInt(3));
    private static final JdbcDataSource JDBC_DATA_SOURCE = new JdbcDataSource();

    static {
        JDBC_DATA_SOURCE.setUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;");
        JDBC_DATA_SOURCE.setUser("");
        JDBC_DATA_SOURCE.setPassword("");
    }

    private final JdbcTemplate jdbcTemplate = new JdbcTemplate(JDBC_DATA_SOURCE);

    @BeforeEach
    void setup() throws SQLException {
        String schema = "create table if not exists cars (\n"
            + "    id bigint auto_increment,\n"
            + "    name varchar(100) not null,\n"
            + "    speed int(11) unsigned not null,\n"
            + "    primary key(id)\n"
            + ");";
        try (Connection conn = JDBC_DATA_SOURCE.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(schema)) {
            pstmt.execute();
        }
    }

    @AfterEach
    void tearDown() throws SQLException {
        String sql = "drop table cars";
        try (Connection conn = JDBC_DATA_SOURCE.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.execute();
        }
    }

    @DisplayName("삽입 쿼리를 실행한다.")
    @Test
    void insert() {
        String insertQuery = "insert into cars (name, speed) values (?, ?)";
        String selectQuery = "select * from cars where id = ?";

        jdbcTemplate.update(insertQuery, "myCar", 120);
        Car car = jdbcTemplate.queryForObject(selectQuery, CAR_ROW_MAPPER, 1L);

        assertAll(
            () -> assertThat(car.getName()).isEqualTo("myCar"),
            () -> assertThat(car.getSpeed()).isEqualTo(120)
        );
    }

    @DisplayName("수정 쿼리를 실행한다.")
    @Test
    void update() {
        String insertQuery = "insert into cars (name, speed) values (?, ?)";
        jdbcTemplate.update(insertQuery, "myCar", 120);
        String updateQuery = "update cars set name =?, speed = ? where id = ?";
        jdbcTemplate.update(updateQuery, "myChangedCar", 100, 1L);

        String selectQuery = "select * from cars where id = ?";
        Car car = jdbcTemplate.queryForObject(selectQuery, CAR_ROW_MAPPER, 1L);

        assertAll(
            () -> assertThat(car.getName()).isEqualTo("myChangedCar"),
            () -> assertThat(car.getSpeed()).isEqualTo(100)
        );
    }

    @DisplayName("여러 항목을 가져오는 쿼리를 실행한다.")
    @Test
    void queryForList() {
        String insertQuery = "insert into cars (name, speed) values (?, ?)";
        jdbcTemplate.update(insertQuery, "car1", 120);
        jdbcTemplate.update(insertQuery, "car2", 80);
        jdbcTemplate.update(insertQuery, "car3", 100);

        String selectListQuery = "select * from cars";
        final List<Car> cars = jdbcTemplate.queryForList(selectListQuery, CAR_ROW_MAPPER);

        assertThat(cars.size()).isEqualTo(3);
    }

    @DisplayName("단일 항목을 가져오는 쿼리를 실행한다.")
    @Test
    void queryForObject() {
        String insertQuery = "insert into cars (name, speed) values (?, ?)";
        jdbcTemplate.update(insertQuery, "car1", 120);
        jdbcTemplate.update(insertQuery, "car2", 80);
        jdbcTemplate.update(insertQuery, "car3", 100);

        String selectListQuery = "select id, name, speed from cars where speed = ?";
        final Car car = jdbcTemplate.queryForObject(selectListQuery, CAR_ROW_MAPPER, 80);

        assertThat(car.getName()).isEqualTo("car2");
    }

    public static class Car {

        private final Long id;
        private final String name;
        private final int speed;

        public Car(Long id, String name, int speed) {
            this.id = id;
            this.name = name;
            this.speed = speed;
        }

        public String getName() {
            return name;
        }

        public int getSpeed() {
            return speed;
        }
    }

}