package nextstep.jdbc;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.BadGrammarJdbcException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.assertj.core.groups.Tuple.tuple;

class JdbcTemplateTest {

    private static JdbcTemplate jdbcTemplate;
    private static JdbcDataSource jdbcDataSource;

    @BeforeAll
    static void beforeAll() {
        jdbcDataSource = new JdbcDataSource();
        jdbcDataSource.setUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;");
        jdbcDataSource.setUser("");
        jdbcDataSource.setPassword("");
        jdbcTemplate = new JdbcTemplate(jdbcDataSource);
        jdbcTemplate.update("create table if not exists users (\n" +
                "    id bigint auto_increment,\n" +
                "    account varchar(100) not null,\n" +
                "    password varchar(100) not null,\n" +
                "    email varchar(100) not null,\n" +
                "    primary key(id)\n" +
                ");");
    }

    @BeforeEach
    void setup() {
        jdbcTemplate.update("delete from users;");
    }

    @Test
    @DisplayName("update 성공")
    void When_good_update_sql_grammer_success() {
        //given
        final String sql = "insert into users (account, password, email) values (?, ?, ?)";

        //when
        jdbcTemplate.update(sql, "account", "password", "email");

        //then
        final String selectSql = "select id, account, password, email from users";
        List<TestUser> users = jdbcTemplate.query(selectSql, rowMapper);
        assertThat(users).hasSize(1);
    }

    @Test
    @DisplayName("잘못된 문법의 sql문으로 update하면 BadGrammarJdbcException발생")
    void When_bad_update_sql_grammer_fail() {
        //given
        final String sql = "insertttt into users (account, password, email) values (?, ?, ?)";

        //when, then
        assertThatThrownBy(() -> jdbcTemplate.update(sql, "account", "password", "email"))
                .isInstanceOf(BadGrammarJdbcException.class);
    }

    @Test
    @DisplayName("query 성공")
    void When_good_query_sql_grammer_success() {
        //given
        final String insertSql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(insertSql, "ac1", "pass1", "email1");
        jdbcTemplate.update(insertSql, "ac2", "pass2", "email2");

        //when
        final String sql = "select id, account, password, email from users";
        final List<TestUser> users = jdbcTemplate.query(sql, rowMapper);

        //then
        assertThat(users).extracting("account", "password")
                .containsExactly(tuple("ac1", "pass1"),
                        tuple("ac2", "pass2"));
    }

    @Test
    @DisplayName("queryForObject 성공")
    void When_good_query_for_object_grammer_success() {
        //given
        final String insertSql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(insertSql, "ac1", "pass1", "email1");

        //when
        final String sql = "select id, account, password, email from users where account = ?";
        final TestUser user = jdbcTemplate.queryForObject(sql, rowMapper, "ac1").get();

        //then
        assertSoftly(softly -> {
            softly.assertThat(user.getAccount()).isEqualTo("ac1");
            softly.assertThat(user.getPassword()).isEqualTo("pass1");
        });
    }

    private final RowMapper<TestUser> rowMapper = rs -> (
            new TestUser(
                    rs.getLong("id"),
                    rs.getString("account"),
                    rs.getString("password"),
                    rs.getString("email")
            )
    );

    private final class TestUser {

        private Long id;
        private final String account;
        private String password;
        private final String email;

        public TestUser(Long id, String account, String password, String email) {
            this.id = id;
            this.account = account;
            this.password = password;
            this.email = email;
        }

        public Long getId() {
            return id;
        }

        public String getAccount() {
            return account;
        }

        public String getPassword() {
            return password;
        }

        public String getEmail() {
            return email;
        }
    }
}
