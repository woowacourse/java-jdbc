package nextstep.jdbc;

import nextstep.jdbc.exception.DataAccessException;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JdbcTemplateTest {

    private static final RowMapper<TestUser> TEST_USER_ROW_MAPPER = rs -> new TestUser(
            rs.getLong(1),
            rs.getString(2),
            rs.getString(3),
            rs.getString(4));

    private static final String password = "password";
    private static final String email = "joy@test.com";

    private JdbcTemplate jdbcTemplate;
    private JdbcDataSource dataSource;

    private int id;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = new JdbcDataSource();
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

        try (Connection conn = dataSource.getConnection()) {
            try (Statement statement = conn.createStatement()) {
                statement.execute(String.join(";\n",
                        schemaSql,
                        getUserInsertSql("joy1", password, email),
                        getUserInsertSql("joy2", "password2", email),
                        getUserInsertSql("jason", "password3", "jason@test.com")
                ));
            }
            try (Statement statement = conn.createStatement()) {
                ResultSet rs = statement.executeQuery("select * from users limit 1");
                rs.next();
                id = rs.getInt("id");
            }
        }

        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    private String getUserInsertSql(String account, String password, String email) {
        return String.format("insert into users (account, password, email) values ('%s', '%s', '%s')",
                account, password, email);
    }

    @AfterEach
    void tearDown() throws SQLException {
        String cleanUpSql = "set referential_integrity false;" +
                "truncate table users;" +
                "set referential_integrity true;";

        try (Connection conn = dataSource.getConnection();
             Statement statement = conn.createStatement()) {
            statement.execute(cleanUpSql);
        }
    }

    @DisplayName("데이터 단건 조회")
    @Test
    void queryOne() {
        // given
        String sql = "select id, account, password, email from users where account = ?";

        // when
        TestUser user = jdbcTemplate.queryOne(sql, TEST_USER_ROW_MAPPER, "joy1");

        // then
        assertThat(user.getAccount()).isEqualTo("joy1");
        assertThat(user.getPassword()).isEqualTo(password);
        assertThat(user.getEmail()).isEqualTo(email);
    }

    @DisplayName("데이터 단건 조회 - 데이터가 없는 경우")
    @Test
    void queryOne_fail1() {
        // given
        String sql = "select id, account, password, email from users where account = ?";

        // when
        // then
        assertThatThrownBy(() -> jdbcTemplate.queryOne(sql, TEST_USER_ROW_MAPPER, "wrong account"))
                .isExactlyInstanceOf(DataAccessException.class)
                .hasMessage("데이터가 존재하지 않음.");
    }

    @DisplayName("데이터 단건 조회 - 데이터가 여러개인 경우")
    @Test
    void queryOne_fail2() {
        // given
        String sql = "select id, account, password, email from users where email = ?";

        // when
        // then
        assertThatThrownBy(() -> jdbcTemplate.queryOne(sql, TEST_USER_ROW_MAPPER, email))
                .isExactlyInstanceOf(DataAccessException.class)
                .hasMessage("데이터가 1개보다 많음.");
    }

    @DisplayName("데이터 복수건 조회")
    @Test
    void queryMany() {
        // given
        String sql = "select id, account, password, email from users where email = ?";

        // when
        List<TestUser> users = jdbcTemplate.queryMany(sql, TEST_USER_ROW_MAPPER, email);

        // then
        assertThat(users).hasSize(2);

        TestUser user1 = users.get(0);
        assertThat(user1.getAccount()).isEqualTo("joy1");
        assertThat(user1.getPassword()).isEqualTo(password);
        assertThat(user1.getEmail()).isEqualTo(email);

        TestUser user2 = users.get(1);
        assertThat(user2.getAccount()).isEqualTo("joy2");
        assertThat(user2.getPassword()).isEqualTo("password2");
        assertThat(user2.getEmail()).isEqualTo(email);
    }

    @Test
    void update() throws SQLException {
        // given
        final String sql = "update users set account = ?, password = ?, email = ? where id = ?";

        // when
        int effectedRowCount = jdbcTemplate.update(sql, "updated account", "updated password", "updated email", id);

        // then
        assertThat(effectedRowCount).isEqualTo(1);

        try (Connection conn = dataSource.getConnection();
             Statement statement = conn.createStatement();
             ResultSet resultSet = statement.executeQuery("select * from users where id = " + id)) {
            resultSet.next();

            assertThat(resultSet.getString("account")).isEqualTo("updated account");
            assertThat(resultSet.getString("password")).isEqualTo("updated password");
            assertThat(resultSet.getString("email")).isEqualTo("updated email");
        }
    }
}
