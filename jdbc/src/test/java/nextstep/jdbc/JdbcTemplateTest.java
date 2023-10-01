package nextstep.jdbc;

import nextstep.jdbc.support.DataSourceConfig;
import nextstep.jdbc.support.DatabasePopulatorUtils;
import nextstep.jdbc.support.TestData;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class JdbcTemplateTest {

    private static final RowMapper<TestData> TEST_DATA_ROW_MAPPER = resultSet -> {
        long id = resultSet.getLong("id");
        String content = resultSet.getString("content");
        int num = resultSet.getInt("num");
        return new TestData(id, content, num);
    };

    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setup() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());

        final TestData testData = new TestData("firstData", 1);
        jdbcTemplate.update("insert into test_data (content, num) values (?, ?)", testData.getContent(), testData.getNum());
    }

    @Nested
    class UpdateTest {

        @Test
        @DisplayName("insert 쿼리 테스트")
        void successInsertQuery() {
            // given
            final TestData testData = new TestData("content", 1);
            final String sql = "insert into test_data (content, num) values (?, ?)";

            // when
            final int updatedRowNum = jdbcTemplate.update(sql, testData.getContent(), testData.getNum());

            // then
            assertThat(updatedRowNum).isEqualTo(1);
        }

        @Test
        @DisplayName("update 쿼리 테스트")
        void successUpdateQuery() {
            // given
            final String sql = "update test_data set content = ?, num = ? where id = ?";

            // when
            int updatedRowNum = jdbcTemplate.update(sql, "new content", 2, 1);

            // then
            assertThat(updatedRowNum).isEqualTo(1);
        }

        @Test
        @DisplayName("파라미터 수가 적은 경우 않으면 예외를 발생시킨다")
        void throwExceptionIfParameterInputIsLessThenQuestionMark() {
            // given
            final TestData testData = new TestData("content", 1);
            final String sql = "insert into test_data (content, num) values (?, ?)";

            // when
            // then
            Assertions.assertThatThrownBy(() -> jdbcTemplate.update(sql, testData.getContent()))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Parameter \"#2\" is not set; SQL statement:");
        }

        @Test
        @DisplayName("파라미터 수가 많은 경우 않으면 예외를 발생시킨다")
        void throwExceptionIfParameterInputIsLargerThenQuestionMark() {
            // given
            final TestData testData = new TestData("content", 1);
            final String sql = "insert into test_data (content, num) values (?, ?)";

            // when
            // then
            Assertions.assertThatThrownBy(() -> jdbcTemplate.update(sql, testData.getContent(), testData.getNum(), "additional-input"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Invalid value \"3\" for parameter \"parameterIndex\"");
        }
    }

    @Nested
    class QueryTest {

        @Test
        @DisplayName("성공 테스트")
        void success() {
            // given
            final String sql = "select id, content, num from test_data where id = ?";

            // when
            List<TestData> actual = jdbcTemplate.query(sql, TEST_DATA_ROW_MAPPER, 1L);

            // then
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(actual).hasSize(1);
                softly.assertThat(actual.get(0).getContent()).isEqualTo("firstData");
            });
        }

        @Test
        @DisplayName("파라미터 수가 적은 경우 않으면 예외를 발생시킨다")
        void throwExceptionIfParameterInputIsLessThenQuestionMark() {
            // given
            final String sql = "select id, content, num from test_data where id = ?";

            // when
            // then
            Assertions.assertThatThrownBy(() -> jdbcTemplate.query(sql, TEST_DATA_ROW_MAPPER))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Parameter \"#1\" is not set; SQL statement:");
        }

        @Test
        @DisplayName("파라미터 수가 많은 경우 않으면 예외를 발생시킨다")
        void throwExceptionIfParameterInputIsLargerThenQuestionMark() {
            // given
            final String sql = "select id, content, num from test_data where id = ?";

            // when
            // then
            Assertions.assertThatThrownBy(() -> jdbcTemplate.query(sql, TEST_DATA_ROW_MAPPER, 1, "additional-data"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Invalid value \"2\" for parameter \"parameterIndex\"");
        }
    }

    @Nested
    class QueryForObjectTest {

        @Test
        @DisplayName("성공 테스트")
        void success() {
            // given
            final String sql = "select id, content, num from test_data where id = ?";

            // when
            Optional<TestData> actual = jdbcTemplate.queryForObject(sql, TEST_DATA_ROW_MAPPER, 1L);

            // then
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(actual).isPresent();
                softly.assertThat(actual.get().getContent()).isEqualTo("firstData");
            });
        }

        @Test
        @DisplayName("조회한 정보가 없는경우")
        void whenDataIsNotExist() {
            // given
            final String sql = "select id, content, num from test_data where id = ?";

            // when
            Optional<TestData> actual = jdbcTemplate.queryForObject(sql, TEST_DATA_ROW_MAPPER, -1L);

            // then
            assertThat(actual).isEmpty();
        }

        @Test
        @DisplayName("해당되는 정보가 많은경우")
        void whenMatchedDataIsMoreThan2() {
            // given
            jdbcTemplate.update("insert into test_data (content, num) values (?, ?)", "new data", 1);
            final String sql = "select id, content, num from test_data where num = ?";

            // when
            // then
            Assertions.assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, TEST_DATA_ROW_MAPPER, 1L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("selected data count is larger than 1");
        }

        @Test
        @DisplayName("파라미터 수가 적은 경우 않으면 예외를 발생시킨다")
        void throwExceptionIfParameterInputIsLessThenQuestionMark() {
            // given
            final String sql = "select id, content, num from test_data where id = ?";

            // when
            // then
            Assertions.assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, TEST_DATA_ROW_MAPPER))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Parameter \"#1\" is not set; SQL statement:");
        }

        @Test
        @DisplayName("파라미터 수가 많은 경우 않으면 예외를 발생시킨다")
        void throwExceptionIfParameterInputIsLargerThenQuestionMark() {
            // given
            final String sql = "select id, content, num from test_data where id = ?";

            // when
            // then
            Assertions.assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, TEST_DATA_ROW_MAPPER, 1, "additional-data"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Invalid value \"2\" for parameter \"parameterIndex\"");
        }
    }
}
