package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import nextstep.jdbc.executor.QueryExecuteResult;
import nextstep.jdbc.mapper.ResultSetToObjectMapper;
import nextstep.jdbc.test.User;
import nextstep.jdbc.util.DatabasePopulatorUtils;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class JdbcTemplateTest {

    private static final Logger log = LoggerFactory.getLogger(JdbcTemplateTest.class);
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

    private void 구구를_DB에_영속화한다() {
        final String sql = "insert into users (account, password, email) values (?, ?, ?)";

        jdbcTemplate.executeInsertOrUpdateOrDelete(sql, pstmt -> {
            pstmt.setString(1, gugu.getAccount());
            pstmt.setString(2, gugu.getPassword());
            pstmt.setString(3, gugu.getEmail());
        });
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
        assertSameUser(user, ACCOUNT, PASSWORD, EMAIL);
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
        assertSameUser(user, ACCOUNT, PASSWORD, EMAIL);
    }

    @DisplayName("단일 조회 기능 다중 인자 테스트")
    @Test
    void queryForOneVarargsTest() {
        //given
        구구를_DB에_영속화한다();
        //when
        String sql = "select * from users where account = ? and email = ? and password = ?";
        User user = jdbcTemplate.queryForObject(sql, USER_MAPPER, ACCOUNT, EMAIL, PASSWORD);
        //then
        assertSameUser(user, ACCOUNT, PASSWORD, EMAIL);
    }

    @DisplayName("전체 조회 기능을 테스트")
    @Test
    void queryForManyTest() {
        //given
        int size = 4;
        for (int i = 0; i < size; i++) {
            구구를_DB에_영속화한다();
        }
        //when
        String sql = "select * from users";
        List<User> users = jdbcTemplate.queryForMany(sql, USER_MAPPER);

        //then
        assertThat(users.size()).isEqualTo(size);
    }

    @DisplayName("전체 조회 기능 다중 인자 테스트")
    @Test
    void queryForManyVarargsTest() {
        //given
        int size = 4;
        for (int i = 0; i < size; i++) {
            구구를_DB에_영속화한다();
        }
        //when
        String sql = "select * from users where account = ? and email = ? and password = ?";
        List<User> users = jdbcTemplate.queryForMany(sql, USER_MAPPER, ACCOUNT, EMAIL, PASSWORD);

        //then
        assertThat(users.size()).isEqualTo(size);
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
        assertSameUser(user, expectedAccount, PASSWORD, expectedEmail);
    }

    @DisplayName("수정 기능 다중 인자 테스트")
    @Test
    void updateMultiVaragsTest() {
        //given
        구구를_DB에_영속화한다();
        String expectedAccount = "wedge";
        String expectedEmail = "fjzjqhdl@gmail.com";

        //when
        User user = 유저를_조회한다();
        long id = user.getId();
        String updateSql = "update users set account=?, email=? where id = ?";
        jdbcTemplate.executeInsertOrUpdateOrDelete(updateSql, expectedAccount, expectedEmail, id);

        //then
        user = 유저를_조회한다();
        assertThat(user.getId()).isEqualTo(id);
        assertSameUser(user, expectedAccount, PASSWORD, expectedEmail);
    }

    @DisplayName("잘못된 SQL이 입력되면 예외를 반환하는 기능 테스트")
    @Test
    void sqlErrorTest() {
        //given
        String sql = "street women fighter = 필수 시청, 빨리 안보면 인생 낭비";
        //when
        //then
        assertThatThrownBy(() -> jdbcTemplate.executeInsertOrUpdateOrDelete(sql))
            .hasMessageContaining("SQL 처리 중 오류가 발생했습니다.");
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
        QueryExecuteResult queryExecuteResult = jdbcTemplate.executeInsertOrUpdateOrDelete(sql);
        log.info("삭제된 row 수 : {}", queryExecuteResult.effectedRow());
    }

    private void assertSameUser(User user, String expectedAccount, String expectedPassword, String expectedEmail) {
        assertThat(user.getAccount()).isEqualTo(expectedAccount);
        assertThat(user.getPassword()).isEqualTo(expectedPassword);
        assertThat(user.getEmail()).isEqualTo(expectedEmail);
    }
}