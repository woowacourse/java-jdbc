package nextstep.jdbc.core;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private static final RowMapper<Dummy> DUMMY_ROW_MAPPER = resultSet -> new Dummy(
            resultSet.getLong("id"),
            resultSet.getString("account")
    );

    @DisplayName("queryForObject 메서드 실행 시 자원이 정상적으로 해제되는지 확인한다.")
    @Test
    void queryForObject_메서드_실행_시_자원이_정상적으로_해제되는지_확인한다() throws SQLException {
        // given
        var dataSource = mock(DataSource.class);
        var jdbcTemplate = new JdbcTemplate(dataSource);

        var connection = mock(Connection.class);
        var preparedStatement = mock(PreparedStatement.class);
        var resultSet = mock(ResultSet.class);

        given(dataSource.getConnection()).willReturn(connection);
        given(connection.prepareStatement(any())).willReturn(preparedStatement);
        given(preparedStatement.executeQuery()).willReturn(resultSet);

        var id = 1L;
        var account = "mat";

        given(resultSet.getLong("id")).willReturn(id);
        given(resultSet.getString("account")).willReturn(account);
        given(resultSet.next()).willReturn( true, false);

        // when
        jdbcTemplate.queryForObject(
                "SELECT id FROM dummy WHERE id = ? AND account = ?", DUMMY_ROW_MAPPER, id, account);

        // then
        verify(preparedStatement).close();
    }

    @DisplayName("update 메서드 실행 시 자원이 정상적으로 해제되는지 확인한다.")
    @Test
    void update_메서드_실행_시_자원이_정상적으로_해제되는지_확인한다() throws SQLException {
        // given
        var dataSource = mock(DataSource.class);
        var jdbcTemplate = new JdbcTemplate(dataSource);

        var connection = mock(Connection.class);
        var preparedStatement = mock(PreparedStatement.class);

        given(dataSource.getConnection()).willReturn(connection);
        given(connection.prepareStatement(any())).willReturn(preparedStatement);

        // when
        jdbcTemplate.update("UPDATE dummy SET account = ? WHERE id = ?", "pat", 1L);

        // then
        verify(preparedStatement).close();
    }

    @DisplayName("execute 메서드 실행 시 자원이 정상적으로 해제되는지 확인한다.")
    @Test
    void execute_메서드_실행_시_자원이_정상적으로_해제되는지_확인한다() throws SQLException {
        // given
        var dataSource = mock(DataSource.class);
        var jdbcTemplate = new JdbcTemplate(dataSource);

        var connection = mock(Connection.class);
        var preparedStatement = mock(PreparedStatement.class);

        given(dataSource.getConnection()).willReturn(connection);
        given(connection.prepareStatement(any())).willReturn(preparedStatement);

        // when
        jdbcTemplate.execute("DELETE FROM dummy");

        // then
        verify(preparedStatement).close();
    }

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
        assertAll(() -> {
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
