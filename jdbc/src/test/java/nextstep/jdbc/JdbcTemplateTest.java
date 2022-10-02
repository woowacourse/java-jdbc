package nextstep.jdbc;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

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

}
