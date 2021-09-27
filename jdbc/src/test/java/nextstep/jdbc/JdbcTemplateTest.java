package nextstep.jdbc;

import nextstep.jdbc.domain.User;
import nextstep.jdbc.exception.DataAccessException;
import nextstep.jdbc.exception.EmptyResultException;
import nextstep.jdbc.exception.ResultSizeExceedException;
import nextstep.jdbc.support.DataSourceConfig;
import nextstep.jdbc.support.DatabasePopulatorUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JdbcTemplateTest {

    DataSource dataSource;
    JdbcTemplate jdbcTemplate;

    RowMapper<User> rowMapper = resultSet -> new User(
            resultSet.getLong("id"),
            resultSet.getString("account"),
            resultSet.getString("password"),
            resultSet.getString("email")
    );

    @BeforeEach
    void setUp() {
        dataSource = DataSourceConfig.getInstance();
        DatabasePopulatorUtils.execute(dataSource, "schema.sql");

        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @AfterEach
    void tearDown() {
        데이터_초기화();
    }

    @DisplayName("update() : 데이터 저장 sql을 실행할 수 있다.")
    @Test
    void insert() {
        // given
        User user = new User("charlie", "secret", "test@test.com");
        String sql = "insert into users(account, password, email) values(?,?,?)";

        // when
        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());

        // then
        User 조회한_유저 = 유저_단일조회(user.getAccount());
        checkSameUser(조회한_유저, user);
    }

    @DisplayName("update() : 데이터 수정 sql을 실행할 수 있다.")
    @Test
    void update() {
        // given
        User user = new User("charlie", "secret", "test@test.com");
        유저_저장한다(user);
        User 조회한_유저 = 유저_단일조회(user.getAccount());
        String updatedPassword = "updateSecret";
        String updatedEmail = "updateEmail@test.com";

        // when
        String sql = "update users set password = ?, email = ? where account = ?";
        jdbcTemplate.update(sql, updatedPassword, updatedEmail, 조회한_유저.getAccount());

        // then
        User 수정_후_조회한_유저 = 유저_단일조회(user.getAccount());
        User expectedUser = new User("charlie", updatedPassword, updatedEmail);
        checkSameUser(수정_후_조회한_유저, expectedUser);
    }

    @DisplayName("update() : 데이터 삭제 sql을 실행할 수 있다.")
    @Test
    void delete() {
        // given
        User user = new User("charlie", "secret", "test@test.com");
        유저_저장한다(user);
        User 조회한_유저 = 유저_단일조회(user.getAccount());

        // when
        String sql = "delete from users where id = ?";
        jdbcTemplate.update(sql, 조회한_유저.getId());

        // then
        List<User> users = 유저_전체조회();
        assertThat(users).isEmpty();
    }

    @DisplayName("update() : 조회 sql을 사용하면 예외가 발생한다.")
    @Test
    void updateWithInvalidSql() {
        // given
        String sql = "select * from users";

        // when then
        assertThatThrownBy(() -> jdbcTemplate.update(sql))
                .isInstanceOf(DataAccessException.class)
                .hasMessage("executeUpdate Database Access Failed");
    }

    @DisplayName("update() : sql의 파라미터('?') 개수에 맞지않게 인자를 주면 예외가 발생한다.")
    @Test
    void updateWithInvalidSqlParameter() {
        // given
        String sql = "insert into users(account, password, email) values(?,?,?)";

        // when then
        assertThatThrownBy(() -> jdbcTemplate.update(sql, "account"))
                .isInstanceOf(DataAccessException.class)
                .hasMessage("executeUpdate Database Access Failed");
    }

    @DisplayName("query() : 조회 sql을 실행해서 여러개의 결과를 조회할 수 있다.")
    @Test
    void query() {
        // given
        User user1 = new User("charlie", "secret", "test@test.com");
        User user2 = new User("charlie2", "secret", "test@test.com");
        유저_저장한다(user1);
        유저_저장한다(user2);

        // when
        String sql = "select * from users";
        List<User> users = jdbcTemplate.query(sql, rowMapper);
        // then
        assertThat(users).hasSize(2);
    }

    @DisplayName("query() : 잘못된 sql로 실행하면 예외가 발생한다.")
    @Test
    void queryWithInvalidSql() {
        // given
        User user1 = new User("charlie", "secret", "test@test.com");
        User user2 = new User("charlie2", "secret", "test@test.com");
        유저_저장한다(user1);
        유저_저장한다(user2);

        // when then
        String sql = "select id, account, password, email frommmmmmm users";
        assertThatThrownBy(() -> jdbcTemplate.query(sql, rowMapper))
                .isInstanceOf(DataAccessException.class)
                .hasMessage("executeQuery Data Access Failed!!");
    }

    @DisplayName("queryForObject() : 조회 sql을 실행해서 단일 결과를 조회한다.")
    @Test
    void queryForObject() {
        // given
        User user1 = new User("charlie", "secret", "test@test.com");
        User user2 = new User("charlie2", "secret", "test@test.com");
        유저_저장한다(user1);
        유저_저장한다(user2);

        // when
        String sql = "select * from users where account = ?";
        User user = jdbcTemplate.queryForObject(sql, rowMapper, user1.getAccount());

        // then
        checkSameUser(user, user1);
    }

    @DisplayName("queryForObject() : 조회 sql을 실행해서 결과가 비어있으면(0개) 예외가 발생한다.")
    @Test
    void queryForObjectResultEmpty() {
        // when then
        String sql = "select * from users where account = ?";
        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, rowMapper, "charlie"))
                .isInstanceOf(EmptyResultException.class)
                .hasMessage("queryForObject Result is Empty");
    }

    @DisplayName("queryForObject() : 조회 sql을 실행해서 결과가 1개를 초과하면 예외가 발생한다.")
    @Test
    void queryForObjectResultSizeOverThan1() {
        // given
        User user1 = new User("charlie", "secret", "test@test.com");
        User user2 = new User("charlie", "secret", "test@test.com");
        유저_저장한다(user1);
        유저_저장한다(user2);

        // when then
        String sql = "select * from users where account = ?";
        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, rowMapper, "charlie"))
                .isInstanceOf(ResultSizeExceedException.class)
                .hasMessage("queryForObject Result Size Over than 1");
    }

    private void 유저_저장한다(User user) {
        String sql = "insert into users(account, password, email) values(?,?,?)";
        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

    private User 유저_단일조회(String account) {
        String sql = "select * from users where account = ?";
        return jdbcTemplate.queryForObject(sql, rowMapper, account);
    }

    private List<User> 유저_전체조회() {
        String sql = "select * from users";
        return jdbcTemplate.query(sql, rowMapper);
    }

    private void 데이터_초기화() {
        String sql = "delete from users";
        jdbcTemplate.update(sql);
    }

    private void checkSameUser(User user, User expectedUser) {
        assertThat(user.getAccount()).isEqualTo(expectedUser.getAccount());
        assertThat(user.getPassword()).isEqualTo(expectedUser.getPassword());
        assertThat(user.getEmail()).isEqualTo(expectedUser.getEmail());
    }
}