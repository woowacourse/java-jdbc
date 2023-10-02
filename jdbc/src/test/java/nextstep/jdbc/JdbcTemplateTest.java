package nextstep.jdbc;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JdbcTemplateTest {

    private static final RowMapper<User> USER_ROW_MAPPER = (rs, rowNum) -> new User(
            rs.getLong(1),
            rs.getString(2),
            rs.getString(3),
            rs.getString(4));

    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setup() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());

        jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
        final String account = "amaranth";
        final String account2 = "myosotis";
        final String insertSql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(insertSql, account, "password", "aaa@aaa");
        jdbcTemplate.update(insertSql, account2, "password", "aaa@aaa");
    }

    @Test
    @DisplayName("update 메서드를 호출하면 DB 데이터를 수정/삽입/삭제하는 쿼리를 실행할 수 있다.")
    void update() {
        //given
        final String newAccount = "amaranth2";
        final String updateSql = "update users set account = ? where id= ?";
        final String querySql = "select id, account, password, email from users where id = ?";

        //when
        jdbcTemplate.update(updateSql, newAccount, 1L);
        final User user = jdbcTemplate.queryForObject(querySql, USER_ROW_MAPPER, 1L).get();

        //then
        assertThat(user.getAccount()).isEqualTo(newAccount);
    }

    @Test
    @DisplayName("queryForObject 메서드를 호출하면 DB 데이터를 조회할 수 있다.")
    void queryForObject() {
        //given
        final String account = "myosotis";
        final String sql = "select id, account, password, email from users where id = ?";

        //when
        final User user = jdbcTemplate.queryForObject(sql, USER_ROW_MAPPER, 2L).get();

        //then
        assertThat(user.getAccount()).isEqualTo(account);
    }

    @Test
    @DisplayName("query 메서드를 호출하면 DB 데이터 리스트를 조회할 수 있다.")
    void query() {
        //given
        final String sql = "select id, account, password, email from users";

        //when
        final List<User> users = jdbcTemplate.query(sql, USER_ROW_MAPPER);

        //then
        assertThat(users).isNotEmpty();
    }
}
