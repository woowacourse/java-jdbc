package org.springframework.jdbc.core;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.RowMapper;
import org.springframework.jdbc.core.config.TestDataSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class JdbcTemplateTest {

    private static final String SELECT_ALL_QUERY = "SELECT * FROM test_data";
    private static final JdbcTemplate jdbcTemplate = new JdbcTemplate(TestDataSource.getInstance());

    private final RowMapper<TestData> rowMapper = resultSet -> new TestData(
            resultSet.getLong("id"),
            resultSet.getString("name")
    );

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("INSERT INTO test_data (name) VALUES ('first')");
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.update("DELETE FROM test_data");
    }

    @Test
    void insert() {
        //given
        final String sql = "INSERT INTO test_data (name) VALUES (?)";
        final String parameter = "test";

        //when
        final int count = jdbcTemplate.update(sql, parameter);

        //then
        final List<TestData> result = jdbcTemplate.query(rowMapper, SELECT_ALL_QUERY);

        assertAll(
                () -> assertThat(count).isOne(),
                () -> assertThat(result).hasSize(2)
        );
    }

    @Test
    void update() {
        //given
        final TestData testData = jdbcTemplate.queryForObject(rowMapper, "SELECT * FROM test_data WHERE name = ?", "first");
        assertThat(testData.getName()).isEqualTo("first");
        final String sql = "UPDATE test_data SET name = ? WHERE id = ?";
        final String changeName = "test";

        //when
        final int count = jdbcTemplate.update(sql, changeName, testData.getId());

        //then
        final TestData changeTestData = jdbcTemplate.queryForObject(rowMapper, "SELECT * FROM test_data WHERE id = ?", testData.getId());

        assertAll(
                () -> assertThat(count).isOne(),
                () -> assertThat(changeTestData.getName()).isEqualTo(changeName)
        );
    }

    @Test
    void delete() {
        //given
        final String sql = "DELETE FROM test_data WHERE name = ?";
        final String parameter = "first";

        //when
        final int count = jdbcTemplate.update(sql, parameter);

        //then
        final List<TestData> result = jdbcTemplate.query(rowMapper, SELECT_ALL_QUERY);

        assertAll(
                () -> assertThat(count).isOne(),
                () -> assertThat(result).hasSize(0)
        );
    }

    @Test
    void query() {
        //when
        final List<TestData> result = jdbcTemplate.query(rowMapper, SELECT_ALL_QUERY);

        //then
        final TestData testData = result.get(0);
        assertAll(
                () -> assertThat(result).hasSize(1),
                () -> assertThat(testData.getName()).isEqualTo("first"),
                () -> assertThat(testData.getId()).isNotNull()
        );
    }

    @Test
    void queryForObject() {
        //given
        final String sql = "SELECT * FROM test_data WHERE name = ?";

        //when
        final TestData testData = jdbcTemplate.queryForObject(rowMapper, sql, "first");

        //then
        assertAll(
                () -> assertThat(testData.getName()).isEqualTo("first"),
                () -> assertThat(testData.getId()).isNotNull()
        );
    }

    class TestData {
        private final Long id;
        private final String name;

        public TestData(final Long id, final String name) {
            this.id = id;
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
