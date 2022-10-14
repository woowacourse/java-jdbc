package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.sql.ResultSet;
import java.util.List;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private static final RowMapper<TestUser> OBJECT_MAPPER = (ResultSet rs) ->
            new TestUser(rs.getLong("id"),
                    rs.getString("account"),
                    rs.getString("password"),
                    rs.getString("email"));

    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        final JdbcDataSource jdbcDataSource = new JdbcDataSource();
        jdbcDataSource.setUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;");
        jdbcDataSource.setUser("");
        jdbcDataSource.setPassword("");
        DatabasePopulatorUtils.execute(jdbcDataSource);
        jdbcTemplate = new JdbcTemplate(jdbcDataSource);
    }

    @AfterEach
    void refresh() {
        jdbcTemplate.update("delete from users");
    }

    @DisplayName("insert 쿼리를 완성시켜 실행시킨다.")
    @Test
    void insert() {
        TestUser user = new TestUser("account", "password", "email");
        final String sql = "insert into users (account, password, email) values (?, ?, ?)";
        KeyHolder keyHolder = new KeyHolder();
        jdbcTemplate.update(sql, keyHolder, user.getAccount(), user.getPassword(), user.getEmail());

        assertThat(keyHolder.getKey()).isEqualTo(1L);
    }

    @DisplayName("데이터 하나만 반환하는 find 쿼리를 완성시켜 실행시킨다.")
    @Test
    void find() {
        TestUser user = new TestUser("account", "password", "email");
        String sql = "insert into users (account, password, email) values (?, ?, ?)";
        KeyHolder keyHolder = new KeyHolder();
        jdbcTemplate.update(sql, keyHolder, user.getAccount(), user.getPassword(), user.getEmail());

        sql = "select id, account, password, email from users where id = ?";

        TestUser result = jdbcTemplate.queryForObject(OBJECT_MAPPER, sql, keyHolder.getKey());

        assertThat(result).isEqualTo(user);
    }

    @DisplayName("데이터 하나만 반환하는 find 쿼리의 결과가 1개가 아니면 예외가 발생한다.")
    @Test
    void find_Exception() {
        String sql = "select id, account, password, email from users where id = ?";
        Long invalidId = 9999L;

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(OBJECT_MAPPER, sql, invalidId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("object size is not one");
    }

    @DisplayName("데이터를 조회하는 finds 쿼리를 완성시켜 실행시킨다.")
    @Test
    void finds() {
        TestUser user = new TestUser("account", "password", "email");
        String sql = "insert into users (account, password, email) values (?, ?, ?)";
        KeyHolder keyHolder = new KeyHolder();
        jdbcTemplate.update(sql, keyHolder, user.getAccount(), user.getPassword(), user.getEmail());

        sql = "select id, account, password, email from users where account = ?";
        List<TestUser> results = jdbcTemplate.query(OBJECT_MAPPER, sql, user.getAccount());

        assertAll(
                () -> assertThat(results.size()).isEqualTo(1),
                () -> assertThat(results.get(0)).isEqualTo(user)
        );
    }

    @DisplayName("sql 관련 예외가 터지면 JdbcExecuteException이 발생한다.")
    @Test
    void execute_Sql_Exception() {
        String sql = "insert into abc (account, password, email) values (?, ?, ?)";
        assertThatThrownBy(() -> jdbcTemplate.update(sql, "account", "password", "email"))
                .isInstanceOf(JdbcExecuteException.class)
                .hasMessage("sql \"" + sql + "\" exception!");
    }

    @DisplayName("update 쿼리를 완성시켜 실행시킨다.")
    @Test
    void update() {
        TestUser user = new TestUser("account", "password", "email");
        String sql = "insert into users (account, password, email) values (?, ?, ?)";
        KeyHolder keyHolder = new KeyHolder();
        jdbcTemplate.update(sql, keyHolder, user.getAccount(), user.getPassword(), user.getEmail());

        sql = "UPDATE users SET account = ?, password = ?, email = ? WHERE id = ?";
        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail(), keyHolder.getKey());
        sql = "select id, account, password, email from users where id = ?";
        TestUser result = jdbcTemplate.queryForObject(OBJECT_MAPPER, sql, keyHolder.getKey());

        assertThat(result).isEqualTo(user);
    }

}
