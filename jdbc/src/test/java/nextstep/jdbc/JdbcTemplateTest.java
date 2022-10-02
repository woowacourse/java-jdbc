package nextstep.jdbc;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private JdbcTemplate jdbcTemplate;
    private RowMapper rowMapper = (rs, rowNum) -> rs.getString(rowNum);

    @BeforeEach
    void setUp() {
        jdbcTemplate = new JdbcTemplate(DataSourceConfigForTest.getInstance());
    }

    @AfterEach
    void setDown() {
        DataSourceConfigForTest.truncate();
    }

    @Nested
    @DisplayName("queryForObject()는")
    class queryForObject {

        @Test
        @DisplayName("데이터가 정상적으로 조회 된 경우 이를 반환한다.")
        void queryForObject() {
            Object result = jdbcTemplate.queryForObject(
                "select column_test from jdbc_tests where column_test=?",
                new Object[] {"a"},
                rowMapper);

            assertAll(
                () -> assertThat(result).isInstanceOf(String.class),
                () -> assertThat((String)result).isEqualTo("a")
            );
        }

        @Test
        @DisplayName("데이터가 존재하지 않는 경우 예외가 발생한다.")
        void noData() {
            assertThatThrownBy(
                () -> jdbcTemplate.queryForObject(
                    "select column_test from jdbc_tests where column_test=?",
                    new Object[] {"c"},
                    rowMapper))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("쿼리 결과가 존재하지 않습니다.");
        }

        @Test
        @DisplayName("데이터가 2개 이상 존재하는 경우 예외가 발생한다.")
        void moreThanTwo() {
            assertThatThrownBy(
                () -> jdbcTemplate.queryForObject(
                    "select column_test from jdbc_tests",
                    new Object[] {},
                    rowMapper))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("쿼리 결과가 2개 이상입니다.");
        }

    }

    @Nested
    @DisplayName("query()는")
    class query {

        @Test
        @DisplayName("데이터가 정상적으로 조회 된 경우 이를 반환한다.")
        void query() {
            List<Object> result = jdbcTemplate.query(
                "select column_test from jdbc_tests",
                new Object[] {},
                rowMapper);

            assertAll(
                () -> assertThat(result).hasSize(2),
                () -> assertThat(result.get(0)).isInstanceOf(String.class),
                () -> assertThat(result).containsOnly("a", "b")
            );
        }

        @Test
        @DisplayName("데이터가 존재하지 않는 경우 null이 아닌 빈 배열을 반환한다.")
        void noData() {
            List<Object> result = jdbcTemplate.query(
                "select column_test from jdbc_tests where column_test=?",
                new Object[] {"c"},
                rowMapper);

            assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result).isEmpty()
            );
        }

    }

    @Nested
    @DisplayName("update()는")
    class update {

        @Test
        @DisplayName("파라미터 정보를 올바르게 가져와 실행시킨다.")
        void withParameters() {
            jdbcTemplate.update("insert into jdbc_tests values ('d')", new Object[] {});

            Object result = jdbcTemplate.queryForObject(
                "select column_test from jdbc_tests where column_test=?",
                new Object[] {"d"},
                rowMapper);

            assertAll(
                () -> assertThat(result).isInstanceOf(String.class),
                () -> assertThat((String)result).isEqualTo("d")
            );
        }

        @Test
        @DisplayName("파라미터가 없어도 정상적으로 실행된다.")
        void withoutParameters() {
            jdbcTemplate.update("delete from jdbc_tests where column_test='c'", new Object[] {});

            assertThatThrownBy(
                () -> jdbcTemplate.queryForObject(
                    "select column_test from jdbc_tests where column_test=?",
                    new Object[] {"c"},
                    rowMapper))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("쿼리 결과가 존재하지 않습니다.");
        }
    }

}
