package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Optional;
import nextstep.common.DatabasePopulatorUtils;
import nextstep.common.FakePreparedStatementSetter;
import nextstep.common.FakeRowMapper;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private JdbcDataSource jdbcDataSource;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcDataSource = new JdbcDataSource();
        jdbcDataSource.setUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;");
        jdbcDataSource.setUser("");
        jdbcDataSource.setPassword("");

        DatabasePopulatorUtils.execute(jdbcDataSource);
        jdbcTemplate = new JdbcTemplate(jdbcDataSource);
    }

    @DisplayName("update 메서드가 정상적으로 종료되면 1을 반환한다.")
    @Test
    void update_메서드가_정상적으로_종료되면_connection_prepareStatement_자원을_해제한다() {
        int update = jdbcTemplate.update(new FakePreparedStatementSetter());

        assertThat(update).isEqualTo(1);
    }

    @DisplayName("query 메서드가 정상적으로 종료되면 List를 반환한다.")
    @Test
    void qeury_메서드가_정상적으로_종료되면_List를_반환한다() {
        String sql = "select * from users";
        List<Object> actual = jdbcTemplate.query(sql, new FakeRowMapper());

        assertThat(actual).isNotNull();
    }

    @DisplayName("queryForObject 메서드가 정상적으로 종료되면 데이터를 반환한다.")
    @Test
    void queryForObject_메서드가_정상적으로_종료되면_Optional_객체를_반환한다() {
        jdbcTemplate.update(new FakePreparedStatementSetter());
        String sql = "select * from users where id = ?";
        Long id = 1L;

        Object actual = jdbcTemplate.queryForObject(sql, new FakeRowMapper(), id);

        assertThat(actual).isNotNull();
    }

    @DisplayName("queryForObject 메서드로 존재하지 않는 데이터를 조회하면 예외를 발생한다.")
    @Test
    void queryForObject_메서드로_존재하지_않는_데이터를_조회하면_예외를_발생한다() {
        String sql = "select * from users where id = ?";
        Long id = -1L;

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, new FakeRowMapper(), id))
                .isInstanceOf(RuntimeException.class);
    }
}
