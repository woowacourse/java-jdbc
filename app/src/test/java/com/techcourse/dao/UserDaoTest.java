package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class UserDaoTest {

    private UserDao userDao;

    @BeforeEach
    void setup() throws SQLException {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        clearUsers();

        userDao = new UserDao(DataSourceConfig.getInstance());
        userDao.insert(new User("gugu", "password", "hkkang@woowahan.com"));
    }

    @Test
    @DisplayName("전체 조회는 저장된 모든 사용자의 정보를 반환한다")
    void findAll() {
        // given
        userDao.insert(new User("other", "otherPassword", "other@example.com"));

        // when
        final var users = userDao.findAll();

        // then
        assertThat(users)
                .extracting(User::getId, User::getAccount, User::getPassword, User::getEmail)
                .containsExactlyInAnyOrder(
                        tuple(1L, "gugu", "password", "hkkang@woowahan.com"),
                        tuple(2L, "other", "otherPassword", "other@example.com"));
    }

    @Test
    @DisplayName("ID로 조회하면 해당 사용자의 모든 정보를 반환한다")
    void findById() {
        // given
        final var id = 1L;

        // when
        final var user = userDao.findById(id);

        // then
        assertThat(user).usingRecursiveComparison()
                .isEqualTo(new User(1L, "gugu", "password", "hkkang@woowahan.com"));
    }

    @Test
    @DisplayName("계정으로 조회하면 해당 사용자의 모든 정보를 반환한다")
    void findByAccount() {
        // given
        final var account = "gugu";

        // when
        final var user = userDao.findByAccount(account);

        // then
        assertThat(user).usingRecursiveComparison()
                .isEqualTo(new User(1L, "gugu", "password", "hkkang@woowahan.com"));
    }

    @Test
    @DisplayName("등록한 사용자의 모든 정보를 다시 조회할 수 있다")
    void insert() {
        // given
        final var user = new User("insert-gugu", "password", "hkkang@woowahan.com");

        // when
        userDao.insert(user);

        // then
        final var actual = userDao.findById(2L);
        assertThat(actual).usingRecursiveComparison()
                .isEqualTo(new User(2L, "insert-gugu", "password", "hkkang@woowahan.com"));
    }

    @Test
    @DisplayName("비밀번호 변경을 저장하면 대상 사용자에게만 반영된다")
    void update() {
        // given
        userDao.insert(new User("other", "otherPassword", "other@example.com"));
        final var user = userDao.findById(2L);
        user.changePassword("password99");

        // when
        userDao.update(user);

        // then
        final var actual = userDao.findById(2L);
        assertThat(actual).usingRecursiveComparison()
                .isEqualTo(new User(2L, "other", "password99", "other@example.com"));
        assertThat(userDao.findById(1L).getPassword()).isEqualTo("password");
    }

    @Test
    @DisplayName("저장된 사용자가 없으면 빈 목록을 반환한다")
    void findAllWhenEmpty() throws SQLException {
        // given
        clearUsers();

        // when
        final var users = userDao.findAll();

        // then
        assertThat(users).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 ID로 조회하면 null을 반환한다")
    void findByMissingId() {
        // given
        final var missingId = 999L;

        // when
        final var user = userDao.findById(missingId);

        // then
        assertThat(user).isNull();
    }

    @Test
    @DisplayName("존재하지 않는 계정으로 조회하면 null을 반환한다")
    void findByMissingAccount() {
        // given
        final var missingAccount = "missing";

        // when
        final var user = userDao.findByAccount(missingAccount);

        // then
        assertThat(user).isNull();
    }

    @Test
    @DisplayName("작은따옴표가 포함된 계정도 그대로 저장하고 조회한다")
    void findAccountContainingQuote() {
        // given
        userDao.insert(new User("o'connor", "quotedPassword", "quoted@example.com"));

        // when
        final var user = userDao.findByAccount("o'connor");

        // then
        assertThat(user).usingRecursiveComparison()
                .isEqualTo(new User(2L, "o'connor", "quotedPassword", "quoted@example.com"));
    }

    @Test
    @DisplayName("전달한 JdbcTemplate으로 사용자를 저장하고 조회한다")
    void useProvidedJdbcTemplate() {
        // given
        final var template = new JdbcTemplate(DataSourceConfig.getInstance());
        final var dao = new UserDao(template);
        final var user = new User("template", "templatePassword", "template@example.com");

        // when
        dao.insert(user);

        // then
        assertThat(dao.findByAccount("template")).usingRecursiveComparison()
                .isEqualTo(new User(2L, "template", "templatePassword", "template@example.com"));
    }

    private void clearUsers() throws SQLException {
        try (final var connection = DataSourceConfig.getInstance().getConnection();
             final var statement = connection.createStatement()) {
            statement.execute("truncate table users restart identity");
        }
    }
}
