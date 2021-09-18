package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import nextstep.jdbc.mapper.ResultSetToObjectMapper;
import nextstep.jdbc.test.DatabasePopulatorUtils;
import nextstep.jdbc.test.User;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private static final ResultSetToObjectMapper<User> USER_MAPPER = rs ->
        new User(
            rs.getLong(1),
            rs.getString(2),
            rs.getString(3),
            rs.getString(4)
        );

    private static JdbcTemplate jdbcTemplate;

    private static final String ACCOUNT = "gugu";
    private static final String PASSWORD = "password";
    private static final String EMAIL = "hkkang@woowahan.com";

    private User gugu = new User(ACCOUNT, PASSWORD, EMAIL);

    @BeforeAll
    static void setup() {
        final JdbcDataSource jdbcDataSource = new JdbcDataSource();
        jdbcDataSource.setUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;");
        jdbcDataSource.setUser("");
        jdbcDataSource.setPassword("");
        DatabasePopulatorUtils.execute(jdbcDataSource);

        jdbcTemplate = new JdbcTemplate(jdbcDataSource);
    }

    @AfterEach
    void tearDown() {
        deleteAll();
    }

    @DisplayName("입력 기능을 테스트")
    @Test
    void pstmtInsertTest() {
        //given
        //when
        구구를_DB에_영속화한다();

        //then
        List<User> users = findAll();
        assertThat(users).hasSize(1);

        User user = users.get(0);
        assertThat(user.getAccount()).isEqualTo(ACCOUNT);
        assertThat(user.getPassword()).isEqualTo(PASSWORD);
        assertThat(user.getEmail()).isEqualTo(EMAIL);
    }

    private void 구구를_DB에_영속화한다() {
        final String sql = "insert into users (account, password, email) values (?, ?, ?)";

        jdbcTemplate.executeInsertOrUpdateOrDelete(sql, pstmt -> {
            pstmt.setString(1, gugu.getAccount());
            pstmt.setString(2, gugu.getPassword());
            pstmt.setString(3, gugu.getEmail());
        });
    }

    @DisplayName("단일 조회 기능을 테스트")
    @Test
    void queryForOneTest() {
        //given
        구구를_DB에_영속화한다();
        구구를_DB에_영속화한다();
        //when
        User user = 유저를_조회한다();
        //then
        assertThat(user.getEmail()).isEqualTo(EMAIL);
        assertThat(user.getAccount()).isEqualTo(ACCOUNT);
        assertThat(user.getPassword()).isEqualTo(PASSWORD);
    }

    @DisplayName("수정 기능을 테스트")
    @Test
    void updateTest() {
        //given
        구구를_DB에_영속화한다();
        String expectedAccount = "wedge";
        String expectedEmail = "fjzjqhdl@gmail.com";

        //when
        User user = 유저를_조회한다();
        long id = user.getId();
        String updateSql = "update users set account=?, email=? where id = ?";
        jdbcTemplate.executeInsertOrUpdateOrDelete(updateSql, preparedStatement -> {
            preparedStatement.setString(1, expectedAccount);
            preparedStatement.setString(2, expectedEmail);
            preparedStatement.setLong(3, id);
        });

        //then
        user = 유저를_조회한다();
        assertThat(user.getId()).isEqualTo(id);
        assertThat(user.getEmail()).isEqualTo(expectedEmail);
        assertThat(user.getAccount()).isEqualTo(expectedAccount);
        assertThat(user.getPassword()).isEqualTo(PASSWORD);
    }

    private User 유저를_조회한다() {
        String sql = "select * from users";
        return jdbcTemplate.queryForObject(sql, USER_MAPPER);
    }

    private List<User> findAll() {
        String sql = "select * from users";
        return jdbcTemplate.queryForMany(sql, USER_MAPPER);
    }

    private void deleteAll() {
        String sql = "delete from users";
        jdbcTemplate.executeInsertOrUpdateOrDelete(sql);
    }
}