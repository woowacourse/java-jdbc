package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.util.List;
import nextstep.config.DataSourceConfig;
import nextstep.support.DatabasePopulatorUtils;
import nextstep.support.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private JdbcTemplate jdbcTemplate;
    private RowMapper<User> rowMapper = (rs, rowNum) ->
        new User(
            rs.getLong(1),
            rs.getString(2),
            rs.getString(3),
            rs.getString(4)
        );

    @BeforeEach
    void setUp() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
        String sql = "insert into users (account, password, email) values (?, ?, ?)";
        int rowNum = jdbcTemplate.update(sql, "너잘", "1234", "너잘@woowahan.com");
    }

    @AfterEach
    void tearDown() {
        String sql = "delete from users";
        jdbcTemplate.update(sql);
    }

    @DisplayName("쿼리를 수행한다.")
    @Test
    void update() {
        // given- when
        String sql = "insert into users (account, password, email) values (?, ?, ?)";

        assertThatCode(() -> jdbcTemplate.update(sql, "joanne", "1234", "joanne@woowahan.com"))
            .doesNotThrowAnyException();
    }

    @DisplayName("쿼리를 수행하며, rowNum을 반환한다.")
    @Test
    void updateReturnsRowNum() {
        // given- when
        String sql = "insert into users (account, password, email) values (?, ?, ?)";

        int rowNum = jdbcTemplate.update(sql, "joanne", "1234", "joanne@woowahan.com");
        assertThat(rowNum).isEqualTo(1);
    }

    @DisplayName("select 쿼리를 수행한다.")
    @Test
    void query() {
        // given
        String sql = "select * from users where account = ?";
        // when
        User user = jdbcTemplate.queryForObject(sql, rowMapper, "너잘");

        // then
        assertThat(user.getAccount()).isEqualTo("너잘");
        assertThat(user.getPassword()).isEqualTo("1234");
        assertThat(user.getEmail()).isEqualTo("너잘@woowahan.com");
    }

    @DisplayName("select 쿼리를 수행한다. - List 반환")
    @Test
    void queryForList() {
        // given
        String sql = "select * from users";
        // when
        List<User> users = jdbcTemplate.queryForList(sql, rowMapper);

        // then
        assertThat(users).hasSize(1);
    }
}
