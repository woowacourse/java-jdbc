package nextstep.jdbc;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class JdbcTemplateTest {

    private JdbcDataSource jdbcDataSource;
    private JdbcTemplate jdbcTemplate;
    private String account;
    private String password;
    private String email;

    @BeforeEach
    void setUp() throws SQLException {
        jdbcDataSource = new JdbcDataSource();
        jdbcDataSource.setUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;");
        jdbcDataSource.setUser("");
        jdbcDataSource.setPassword("");

        final String dropQuery = "drop table if exists users";
        JdbcTestUtils.execute(jdbcDataSource, dropQuery);

        final String createQuery = "create table if not exists users (\n" +
                "    id bigint auto_increment,\n" +
                "    account varchar(100) not null,\n" +
                "    password varchar(100) not null,\n" +
                "    email varchar(100) not null,\n" +
                "    primary key(id)\n" +
                ");";
        JdbcTestUtils.execute(jdbcDataSource, createQuery);

        account = "roki";
        password = "pssword";
        email = "roki@woowa.com";
        final String insertQuery = createUserQuery(account, password, email);
        JdbcTestUtils.execute(jdbcDataSource, insertQuery);

        jdbcTemplate = new JdbcTemplate(jdbcDataSource);
    }

    private String createUserQuery(String account, String password, String email) {
        return String.format("insert into users (account, password, email) values ('%s', '%s', '%s')",
                account, password, email);
    }

    @DisplayName("단일 결과를 조회하는 기능 - 유저가 존재하는 경우")
    @Test
    void testQueryForObjectIfExistUser() {
        //given
        String sql = "select id, account, password, email from users where id = ?";

        //when
        User user = jdbcTemplate.queryForObject(sql, parseUser(), 1L);

        //then
        assertThat(user).isNotNull();
        assertThat(user).usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(new User(0L, account, password, email));
    }

    @DisplayName("단일 결과를 조회하는 기능 - 유저가 존재하지 않은 경우")
    @Test
    void testQueryForObjectIfNotExistUser() {
        //given
        String sql = "select id, account, password, email from users where id = ?";

        //when
        User user = jdbcTemplate.queryForObject(sql, parseUser(), 2L);

        //then
        assertThat(user).isNull();
    }

    @DisplayName("다중 결과를 조회하는 기능 - 유저가 존재하는 경우")
    @Test
    void testQuery() {
        //given
        String guguAccount = "gugu";
        String guguEmail = "gugu@woowa.com";
        String guguPassword = "password";
        JdbcTestUtils.execute(jdbcDataSource, createUserQuery(guguAccount, guguPassword, guguEmail));

        String sql = "select id, account, password, email from users";

        //when
        List<User> users = jdbcTemplate.query(sql, parseUser());

        //then
        assertAll(
                () -> assertThat(users).hasSize(2),
                () -> assertThat(users).usingRecursiveFieldByFieldElementComparatorIgnoringFields("id")
                        .containsAnyOf(new User(1L, account, password, email), new User(2L, guguAccount, guguPassword, guguEmail))
        );
    }

    private RowMapper<User> parseUser() {
        return rs -> new User(rs.getLong("id"), rs.getString("account"),
                rs.getString("password"), rs.getString("email"));
    }

    static class User {

        private Long id;
        private final String account;
        private String password;
        private final String email;

        public User(long id, String account, String password, String email) {
            this.id = id;
            this.account = account;
            this.password = password;
            this.email = email;
        }
    }
}
