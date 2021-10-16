package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private static final JdbcTemplate JDBC_TEMPLATE;

    private final RowMapper<User> rowMapper = rs -> new User(
        rs.getLong(1),
        rs.getString(2),
        rs.getString(3),
        rs.getString(4)
    );

    static {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        JDBC_TEMPLATE = new JdbcTemplate(DataSourceConfig.getInstance());
    }

    @AfterEach
    void clear() {
        JDBC_TEMPLATE.update(
            "truncate table users"
        );
    }

    @DisplayName("레코드를 저장한다.")
    @Test
    void update() {
        User user = new User(1L, "woogie", "123", "bbwwpark@naver.com");

        JDBC_TEMPLATE.update(
            "insert into users (id, account, password, email) values (?, ?, ?, ?)",
            user.getId(),
            user.getAccount(),
            user.getPassword(),
            user.getEmail()
        );

        List<User> results = JDBC_TEMPLATE.queryForList(
            "select id, account, password, email from users",
            rowMapper
        );

        assertThat(results.get(0)).isEqualTo(user);
    }

    @DisplayName("레코드를 단건 조회한다.")
    @Test
    void queryForObject() {
        User user = new User(1L, "woogie", "123", "bbwwpark@naver.com");

        JDBC_TEMPLATE.update(
            "insert into users (id, account, password, email) values (?, ?, ?, ?)",
            user.getId(),
            user.getAccount(),
            user.getPassword(),
            user.getEmail()
        );

        User result = JDBC_TEMPLATE.queryForObject(
            "select id, account, password, email from users",
            rowMapper
        );

        assertThat(result).isEqualTo(user);
    }

    @DisplayName("레코드를 다건 조회한다.")
    @Test
    void queryForList() {
        User woogie = new User(3L, "woogie", "123", "bbwwpark@naver.com");

        JDBC_TEMPLATE.update(
            "insert into users (id, account, password, email) values (?, ?, ?, ?)",
            woogie.getId(),
            woogie.getAccount(),
            woogie.getPassword(),
            woogie.getEmail()
        );

        User air = new User(4L, "air", "123", "ex@naver.com");
        JDBC_TEMPLATE.update(
            "insert into users (id, account, password, email) values (?, ?, ?, ?)",
            air.getId(),
            air.getAccount(),
            air.getPassword(),
            air.getEmail()
        );

        List<User> results = JDBC_TEMPLATE.queryForList(
            "select id, account, password, email from users",
            rowMapper
        );

        assertThat(results.size()).isEqualTo(2);
        assertThat(results.get(0)).isEqualTo(woogie);
        assertThat(results.get(1)).isEqualTo(air);
    }

}