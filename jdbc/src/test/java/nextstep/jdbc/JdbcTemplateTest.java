package nextstep.jdbc;

import config.DataSourceConfig;
import init.DatabasePopulatorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JdbcTemplateTest {

    private static final RowMapper<Map<String, Object>> HERB_ROW_MAPPER = rs -> {
        Map<String, Object> herb = new HashMap<>();
        herb.put("id", rs.getLong("id"));
        herb.put("name", rs.getString("name"));
        herb.put("age", rs.getInt("age"));
        return herb;
    };

    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        DataSource dataSource = DataSourceConfig.getInstance();
        DatabasePopulatorUtils.execute(dataSource);
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    void execute() {
        // given
        final String sql = "insert into herb(name, age) values(?, ?)";
        final Object[] args = {"민트", 25};

        // when
        final int affectedRows = jdbcTemplate.execute(sql, args);

        // then
        assertThat(affectedRows).isOne();
    }

    @Test
    void execute_AffectMultipleRows() {
        // given
        insert("민트", 25);
        insert("민트2", 25);
        insert("민트3", 26);
        final String sql = "update herb set name = ? where age = ?";
        final Object[] args = {"민트4", 25};

        // when
        final int affectedRows = jdbcTemplate.execute(sql, args);

        // then
        assertThat(affectedRows).isEqualTo(2);
    }

    private void insert(String name, int age) {
        final String sql = "insert into herb(name, age) values(?, ?)";
        final Object[] args = {name, age};
        jdbcTemplate.execute(sql, args);
    }

    @Test
    void queryForObject() {
        // given
        final String expectedName = "민트";
        final int expectedAge = 25;
        final long expectedId = 1L;

        insert(expectedName, expectedAge);

        final String sql = "select * from herb where id = ?";
        final Object[] args = {expectedId};

        // when
        final Optional<Map<String, Object>> result = jdbcTemplate.queryForObject(sql, HERB_ROW_MAPPER, args);
        final Map<String, Object> actual = result.get();

        // then
        assertThat(actual.get("id")).isEqualTo(expectedId);
        assertThat(actual.get("name")).isEqualTo(expectedName);
        assertThat(actual.get("age")).isEqualTo(expectedAge);
    }

    @Test
    void queryForObject_EmptyResult_ReturnOptionalEmpty() {
        // given
        final String sql = "select * from herb where id = ?";
        final Object[] args = {1L};

        // when
        final Optional<Map<String, Object>> result = jdbcTemplate.queryForObject(sql, HERB_ROW_MAPPER, args);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void queryForObject_MultipleResult_ExceptionThrown() {
        // given
        insert("민트1", 25);
        insert("민트2", 25);
        final String sql = "select * from herb where age = ?";
        final Object[] args = {25};

        // when, then
        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, HERB_ROW_MAPPER, args))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void queryForObject_InvalidSql_RuntimeExceptionThrown() {
        // given
        insert("민트1", 25);
        final String invalidSql = "select * from invalid_table where age = ?";
        final Object[] args = {25};

        // when, then
        assertThatThrownBy(() -> jdbcTemplate.queryForObject(invalidSql, HERB_ROW_MAPPER, args))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void queryForObject_InvalidArguments_RuntimeExceptionThrown() {
        // given
        insert("민트1", 25);
        final String sql = "select * from invalid_table where age = ?";
        final Object[] invalidArgs = {"필요없는 인자", 25};

        // when, then
        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, HERB_ROW_MAPPER, invalidArgs))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void query() {
        // given
        insert("민트1", 25);
        insert("민트2", 25);
        insert("민트3", 25);
        insert("민트4", 25);
        final String sql = "select * from herb where age = ?";
        final Object[] args = {25};

        // when
        final List<Map<String, Object>> results = jdbcTemplate.query(sql, HERB_ROW_MAPPER, args);

        // then
        assertThat(results).hasSize(4);
    }
}
