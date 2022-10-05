package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import javax.sql.DataSource;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import nextstep.jdbc.mock.MockObject;
import nextstep.jdbc.support.DataSourceConfig;
import nextstep.jdbc.support.DatabasePopulatorUtils;

class JdbcTemplateTest {

    private static final RowMapper<MockObject> MOCK_OBJECT_ROW_MAPPER = (resultSet) ->
        new MockObject(
            resultSet.getString("field1"),
            resultSet.getString("field2")
        );

    private DataSource dataSource;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        dataSource = DataSourceConfig.getInstance();
        jdbcTemplate = new JdbcTemplate(dataSource);

        DatabasePopulatorUtils.init(dataSource);
    }

    @AfterEach
    void tearDown() {
        DatabasePopulatorUtils.clear(dataSource);
    }

    @DisplayName("데이터의 목록을 조회하는 쿼리문 실행")
    @Test
    void query() {
        final int counts = 3;
        for (int i = 0; i < counts; i++) {
            jdbcTemplate.update(
                "insert into mock_objects (field1, field2) values (?, ?)", "field1", "field2"
            );
        }

        final String sql = "select * from mock_objects";

        final List<MockObject> mockObjects = jdbcTemplate.query(sql, MOCK_OBJECT_ROW_MAPPER);

        assertThat(mockObjects).hasSize(counts);
    }

    @DisplayName("데이터를 조회하는 쿼리문 실행")
    @Test
    void queryForObject() {
        jdbcTemplate.update(
            "insert into mock_objects (field1, field2) values (?, ?)", "field1", "field2"
        );

        final String field1 = "field1";
        final String sql = "select * from mock_objects where field1 = " + field1;

        final MockObject mockObject = jdbcTemplate.queryForObject(sql, MOCK_OBJECT_ROW_MAPPER);

        assertThat(mockObject.getField1()).isEqualTo(field1);
    }

    @DisplayName("데이터 조회 시 결과값이 없을 경우 예외 발생")
    @Test
    void queryForObjectEmpty() {
        final String sql = "select * from mock_objects where field1 = field1";

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, MOCK_OBJECT_ROW_MAPPER))
            .isInstanceOf(DataAccessException.class);
    }

    @DisplayName("데이터 조회 시 결과값이 여러 개일 경우 예외 발생")
    @Test
    void queryForObjects() {
        for (int i = 0; i < 3; i++) {
            jdbcTemplate.update(
                "insert into mock_objects (field1, field2) values (?, ?)", "field1", "field2"
            );
        }

        final String sql = "select * from mock_objects where field1 = field1";

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, MOCK_OBJECT_ROW_MAPPER))
            .isInstanceOf(DataAccessException.class);
    }

    @DisplayName("데이터 변경이 있는 쿼리문 실행")
    @Test
    void update() {
        final String sql = "insert into mock_objects (field1, field2) values (?, ?)";

        final int rowCount = jdbcTemplate.update(sql, "field1", "field2");

        assertThat(rowCount).isEqualTo(1);
    }

    @DisplayName("SQL 문법에 맞지 않는 쿼리문 실행 시 예외 발생")
    @Test
    void doesNotConformToTheSQLSyntax() {
        final String sql = "invalidSQLQuery";

        assertThatThrownBy(() -> jdbcTemplate.update(sql))
            .isInstanceOf(DataAccessException.class);
    }

    @DisplayName("파라미터 개수에 맞지 않는 파라미터를 입력했을 경우 예외 발생")
    @Test
    void doesNotMatchTheNumberOfParameters() {
        final String sql = "insert into mock_objects (field1, field2) values (?, ?)";

        assertThatThrownBy(() -> jdbcTemplate.update(sql, "field1", "field2", "field3"))
            .isInstanceOf(DataAccessException.class);
    }
}
