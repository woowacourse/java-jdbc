package nextstep.jdbc;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.BadGrammarJdbcException;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

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
    @DisplayName("맞는 문법의 sql문으로 update하면 성공")
    void When_good_sql_grammer_success() {
        final String sql = "insert into users (account, password, email) values (?, ?, ?)";

        assertDoesNotThrow(() -> jdbcTemplate.update(sql, "account", "password", "email"));
    }

    @Test
    @DisplayName("잘못된 문법의 sql문으로 update하면 BadGrammarJdbcException발생")
    void When_bad_sql_grammer_fail() {
        final String sql = "insertttt into users (account, password, email) values (?, ?, ?)";

        assertThatThrownBy(() -> jdbcTemplate.update(sql, "account", "password", "email"))
                .isInstanceOf(BadGrammarJdbcException.class);
    }
}
