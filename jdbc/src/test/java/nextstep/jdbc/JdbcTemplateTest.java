package nextstep.jdbc;

import nextstep.jdbc.exception.DataAccessException;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JdbcTemplateTest {

    private static final RowMapper<TestUser> TEST_USER_ROW_MAPPER = rs -> new TestUser(
            rs.getLong(1),
            rs.getString(2),
            rs.getString(3),
            rs.getString(4));

    private JdbcTemplate jdbcTemplate;

    private final String account = "joy";
    private final String password = "password";
    private final String email = "joy@test.com";


    @BeforeEach
    void setUp() throws SQLException {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;");
        dataSource.setUser("");
        dataSource.setPassword("");

        final String schemaSql = "create table if not exists users (\n" +
                "    id bigint auto_increment,\n" +
                "    account varchar(100) not null,\n" +
                "    password varchar(100) not null,\n" +
                "    email varchar(100) not null,\n" +
                "    primary key(id)\n" +
                ");";
        final String insertSql = String.format("insert into users (account, password, email) values ('%s', '%s', '%s')",
                account, password, email);

        try (Connection conn = dataSource.getConnection()) {
            try (Statement statement = conn.createStatement()) {
                statement.execute(schemaSql);
            }
            try (Statement statement = conn.createStatement()) {
                statement.execute(insertSql);
            }
        }

        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @DisplayName("데이터 조회")
    @Test
    void queryOne() {
        String sql = "select id, account, password, email from users where account = ?";

        TestUser user = jdbcTemplate.queryOne(sql, TEST_USER_ROW_MAPPER, account);

        assertThat(user.getAccount()).isEqualTo(account);
        assertThat(user.getPassword()).isEqualTo(password);
        assertThat(user.getEmail()).isEqualTo(email);
    }

    @DisplayName("데이터가 없는 경우")
    @Test
    void queryOne_fail() {
        String sql = "select id, account, password, email from users where account = ?";

        assertThatThrownBy(() -> jdbcTemplate.queryOne(sql, TEST_USER_ROW_MAPPER, "wrong account"))
                .isExactlyInstanceOf(DataAccessException.class);
    }
}
