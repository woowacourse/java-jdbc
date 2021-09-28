package nextstep.jdbc;

import java.util.List;
import nextstep.config.DataSourceConfig;
import nextstep.jdbc.exception.IncorrectResultSizeDataAccessException;
import nextstep.support.DatabasePopulatorUtils;
import nextstep.support.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.*;

class JdbcTemplateTest {
    private JdbcTemplate jdbcTemplate;
    private final RowMapper<User> rowMapper = resultSet ->
            new User(
                    resultSet.getLong("id"),
                    resultSet.getString("account"),
                    resultSet.getString("password"),
                    resultSet.getString("email")
            );

    @BeforeEach
    void setUp() {
        DataSource dataSource = DataSourceConfig.getInstance();
        DatabasePopulatorUtils.execute(dataSource);
        jdbcTemplate = new JdbcTemplate(dataSource);
        insertUser("코다", "password", "코다@gmail.com");
    }

    @AfterEach
    void after() {
        String sql = "delete from users";
        jdbcTemplate.update(sql);
    }

    @DisplayName("insert 쿼리 실행")
    @Test
    void updateTest() {
        String sql = "insert into users (account, password, email) values (?, ?, ?)";

        jdbcTemplate.update(sql, "코다", "password", "코다@gmail.com");

        assertThatCode(() -> jdbcTemplate.update(sql, "코다", "password", "코다@gmail.com"))
                .doesNotThrowAnyException();
    }

    @DisplayName("하나의 객체 반환 - queryForObject")
    @Test
    void queryTest() {
        String sql = "select * from users where account = ?";
        User user = jdbcTemplate.queryForObject(sql, rowMapper, "코다");

        assertThat(user.getAccount()).isEqualTo("코다");
        assertThat(user.getPassword()).isEqualTo("password");
        assertThat(user.getEmail()).isEqualTo("코다@gmail.com");
    }

    @DisplayName("반환값이 1개 이상일 때 queryForObject 예외 발생 - IncorrectResultSizeDataAccessException")
    @Test
    void queryExceptionTest() {
        insertUser("코다", "password2", "코다2@gmail.com");

        String sql = "select * from users where account = ?";

        assertThatThrownBy(() -> {
            jdbcTemplate.queryForObject(sql, rowMapper, "코다");
        }).isInstanceOf(IncorrectResultSizeDataAccessException.class);
    }

    @DisplayName("List 반환 - query")
    @Test
    void queryForListTest() {
        String sql = "select * from users";
        List<User> users = jdbcTemplate.query(sql, rowMapper);

        assertThat(users).hasSize(1);
    }

    private void insertUser(String account, String password, String email) {
        String sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(sql, account, password, email);
    }
}
