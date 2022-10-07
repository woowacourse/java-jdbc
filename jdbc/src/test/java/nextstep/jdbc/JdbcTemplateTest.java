package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import nextstep.jdbc.config.DataSourceConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private final RowMapper<User> userRowMapper = rs -> new User(
            rs.getLong("id"),
            rs.getString("account"),
            rs.getString("password"),
            rs.getString("email")
    );

    private final JdbcTemplate jdbcTemplate;

    public JdbcTemplateTest() {
        jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());

        jdbcTemplate.update("create table if not exists users ( "
                + "    id bigint auto_increment, "
                + "    account varchar(100) not null, "
                + "    password varchar(100) not null, "
                + "    email varchar(100) not null, "
                + "    primary key(id) "
                + ");");
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.update("delete from users");
    }

    @DisplayName("update는 sql 쿼리를 실행시킨다")
    @Test
    void update() {
        // given
        final String sql = "insert into users (account, password, email) values (?, ?, ?)";
        final String account = "alien";
        final String password = "password";
        final String email = "alien@mail.com";

        // when
        jdbcTemplate.update(sql, account, password, email);

        // then
        final String findSql = "select id, account, password, email from users";
        final User user = jdbcTemplate.queryForObject(findSql, userRowMapper);
        assertThat(user.getAccount()).isEqualTo(account);
    }

    @DisplayName("queryForObject는 한개의 결과를 반환한다.")
    @Test
    void queryForObject() {
        // given
        final String account = "alien";
        final String password = "password";
        final String email = "alien@mail.com";
        insertUser(account, password, email);

        final var sql = "select id, account, password, email from users where account = ?";

        // when
        final User user = jdbcTemplate.queryForObject(sql, userRowMapper, account);

        // then
        assertThat(user.getAccount()).isEqualTo(account);
    }

    @DisplayName("queryForObject는 결과가 없으면 예외를 던진다.")
    @Test
    void queryForObjectWithEmpty() {
        // when & then
        final var sql = "select id, account, password, email from users where account = ?";

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, userRowMapper, "alien"))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining("데이터가 없습니다.");
    }

    @DisplayName("queryForObject는 결과가 두개 이상이면 예외가 던진다.")
    @Test
    void queryForObjectWithMultiData() {
        // given
        insertUser("alien", "password", "alien@mail.com");
        insertUser("gugu", "password", "gugu@mail.com");

        final var sql = "select id, account, password, email from users";

        // when & then
        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, userRowMapper))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining("데이터가 한개가 아닙니다.");
    }

    @DisplayName("query는 결과를 List형으로 반환한다.")
    @Test
    void query() {
        // given
        insertUser("alien", "password", "alien@mail.com");
        insertUser("gugu", "password", "gugu@mail.com");

        final var sql = "select id, account, password, email from users";

        // when
        final List<User> users = jdbcTemplate.query(sql, userRowMapper);

        // then
        assertThat(users).hasSize(2);
    }

    private void insertUser(final String account, final String password, final String email) {
        final String sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(sql, account, password, email);
    }

    static class User {

        private Long id;
        private final String account;
        private String password;
        private final String email;

        public User(final long id, final String account, final String password, final String email) {
            this.id = id;
            this.account = account;
            this.password = password;
            this.email = email;
        }

        public User(final String account, final String password, final String email) {
            this.account = account;
            this.password = password;
            this.email = email;
        }

        public boolean checkPassword(final String password) {
            return this.password.equals(password);
        }

        public void changePassword(final String password) {
            this.password = password;
        }

        public String getAccount() {
            return account;
        }

        public long getId() {
            return id;
        }

        public String getEmail() {
            return email;
        }

        public String getPassword() {
            return password;
        }
    }
}
