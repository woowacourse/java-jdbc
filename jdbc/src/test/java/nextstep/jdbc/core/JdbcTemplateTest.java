package nextstep.jdbc.core;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private static final RowMapper<Dummy> DUMMY_ROW_MAPPER = resultSet -> new Dummy(
            resultSet.getLong("id"),
            resultSet.getString("account")
    );

    @DisplayName("sql문과 RowMapper 구현체를 전달하면 List가 반환된다.")
    @Test
    void sql문과_RowMapper_구현체를_전달하면_List가_반환된다() {
        // given
        var jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.query(any(), any()))
                .thenReturn(List.of(new Dummy(1L, "pat"), new Dummy(2L, "mat")));

        var sql = "SELECT id, account FROM dummy";

        // when
        var actual = jdbcTemplate.query(sql, DUMMY_ROW_MAPPER);

        // then
        assertThat(actual).hasSize(2);
    }

    @DisplayName("단일 객체를 반환한다.")
    @Test
    void 단일_객체를_반환한다() {
        // given
        var jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.queryForObject(any(), any(), any()))
                .thenReturn(new Dummy(1L, "릭"));

        var sql = "SELECT id, account FROM dummy WHERE id = ?";

        // when
        var actual = jdbcTemplate.queryForObject(sql, DUMMY_ROW_MAPPER, 1L);

        // then
        Assertions.assertAll(() -> {
            assertThat(actual.getId()).isEqualTo(1L);
            assertThat(actual.getAccount()).isEqualTo("릭");
        });
    }

    @DisplayName("update를 진행한다.")
    @Test
    void update를_진행한다() {
        var jdbcTemplate = mock(JdbcTemplate.class);

        doNothing()
                .when(jdbcTemplate)
                .update(any(), any());

        var sql = "UPDATE dummy SET account = ? WHERE id = ?";

        // when & then
        assertDoesNotThrow(() -> jdbcTemplate.update(sql, "pat", 1L));
    }

    @DisplayName("단순 sql문을 실행한다.")
    @Test
    void 단순_sql문을_실행한다() {
        // given
        var jdbcTemplate = mock(JdbcTemplate.class);

        doNothing()
                .when(jdbcTemplate)
                .execute(any());

        var sql = "DELETE FROM dummy";

        // when & then
        assertDoesNotThrow(() -> jdbcTemplate.execute(sql));
    }

    private static class Dummy {
        private final Long id;
        private final String account;

        public Dummy(final Long id, final String account) {
            this.id = id;
            this.account = account;
        }

        public Long getId() {
            return id;
        }

        public String getAccount() {
            return account;
        }
    }
}
