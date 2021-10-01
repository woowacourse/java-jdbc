package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import nextstep.config.DataSourceConfig;
import nextstep.config.DatabasePopulatorUtils;
import nextstep.domain.User;
import nextstep.exception.SqlQueryException;
import nextstep.exception.SqlUpdateException;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private static final String INSERT_QUERY = "insert into users (account, password, email) values (?, ?, ?)";

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<User> rowMapper =
        resultSet -> new User(
            resultSet.getLong(1),
            resultSet.getString(2),
            resultSet.getString(3),
            resultSet.getString(4));

    public JdbcTemplateTest() {
        jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
    }

    @Test
    @DisplayName("삽입, 삭제와 같은 과정을 수행한다.")
    void update() {
        String info = "test1";

        ThrowingCallable callable = () -> jdbcTemplate.update(INSERT_QUERY, info, info, info);

        assertThatCode(callable).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("삽입, 삭제와 같은 과정을 수행한다.")
    void updateWhenException() {
        String sql = "update users set account = ?, password = ?, email = ? where id = ?";
        String info = "test2";

        ThrowingCallable callable = () -> jdbcTemplate.update(sql, info, info, info);

        assertThatThrownBy(callable).isExactlyInstanceOf(SqlUpdateException.class);
    }

    @Test
    @DisplayName("유저 오브젝트 1개를 가져온다.")
    void queryForObject() {
        String sql = "select id, account, password, email from users where account = ?";
        String info = "test3";
        jdbcTemplate.update(INSERT_QUERY, info, info, info);

        User user = jdbcTemplate.queryForObject(sql, rowMapper, info).orElseThrow();

        assertThat(user.getAccount()).isEqualTo(info);
    }

    @Test
    @DisplayName("가져올 오브젝트가 여러개라면 에러가 발생한다.")
    void queryForObjectWhenNotSingleSize() {
        String sql = "select id, account, password, email from users where account = ?";
        String info = "test4";
        jdbcTemplate.update(INSERT_QUERY, info, info, info);
        jdbcTemplate.update(INSERT_QUERY, info, info, info);

        ThrowingCallable callable = () -> jdbcTemplate.queryForObject(sql, rowMapper, info);

        assertThatThrownBy(callable).isExactlyInstanceOf(SqlQueryException.class);
    }

    @Test
    @DisplayName("여러개의 오브젝트를 가져온다.")
    void query() {
        String sql = "select id, account, password, email from users";
        String info = "test5";
        jdbcTemplate.update(INSERT_QUERY, info, info, info);
        jdbcTemplate.update(INSERT_QUERY, info, info, info);

        List<User> users = jdbcTemplate.query(sql, rowMapper);

        assertThat(users.size()).isNotZero();
    }
}