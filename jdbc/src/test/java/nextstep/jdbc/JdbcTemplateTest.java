package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.stream.Collectors;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setup() throws Exception {
        final JdbcDataSource jdbcDataSource = new JdbcDataSource();
        jdbcDataSource.setURL("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;");
        jdbcDataSource.setUser("");
        jdbcDataSource.setPassword("");

        DatabasePopulatorUtils.execute(jdbcDataSource);

        this.jdbcTemplate = new JdbcTemplate(jdbcDataSource);
    }

    @Test
    @DisplayName("select 쿼리를 이용해 지정한 객체로 불러올 수 있다.")
    void queryForObject() throws Exception{
        final String sql = "select * from users where id = ?";

        final User user = (User) jdbcTemplate.queryForObject(sql, User.class, 1L);

        assertThat(user.getUsername()).isEqualTo("test");
    }

    @Test
    @DisplayName("select 쿼리를 이용해 지정한 클래스의 객체 List를 불러올 수 있다.")
    void query() {
        final String sql = "select * from users";

        final List<User> users = jdbcTemplate.query(sql, User.class)
                .stream()
                .map(User.class::cast)
                .collect(Collectors.toList());

        assertThat(users).hasSize(2);
    }

    @Test
    @DisplayName("insert 쿼리를 이용해 데이터를 삽입할 수 있다.")
    void update_Insert() {
        final String sql = "insert into users (username) values (?)";

        jdbcTemplate.update(sql, "east");

        final String selectSql = "select * from users where id = ?";
        final User user = (User) jdbcTemplate.queryForObject(selectSql, User.class, 3L);
        assertThat(user.getUsername()).isEqualTo("east");
    }

    @Test
    @DisplayName("update 쿼리를 이용해 데이터를 수정할 수 있다.")
    void update_Update() {
        final String sql = "update users set username = ? where id = ?";

        jdbcTemplate.update(sql, "east", 1L);

        final String selectSql = "select * from users where id = ?";
        final User user = (User) jdbcTemplate.queryForObject(selectSql, User.class, 1L);
        assertThat(user.getUsername()).isEqualTo("east");
    }

    @Test
    void update_Delete() {
        final String sql = "delete from users where id = ?";

        jdbcTemplate.update(sql, 1L);
        jdbcTemplate.update(sql, 2L);

        final String selectSql = "select * from users where id = ?";
        assertThatThrownBy(() -> jdbcTemplate.queryForObject(selectSql, User.class, 1L))
                .isInstanceOf(RuntimeException.class);
    }

    static class User {
        private long id;
        private String username;

        public User(long id, String username) {
            this.id = id;
            this.username = username;
        }

        public User(String username) {
            this.username = username;
        }

        public Long getId() {
            return id;
        }

        public String getUsername() {
            return username;
        }
    }
}
