package com.techcourse.dao;

import static org.assertj.core.api.Assertions.assertThat;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserDaoTest {

    private UserDao userDao;

    @BeforeEach
    void setup() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());

        userDao = new UserDao();
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @AfterEach
    void tearDown() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
        jdbcTemplate.executeUpdate("""
                delete from users;
                alter table users alter column id restart with 1;
                """);
    }

    @DisplayName("전체 조회")
    @Test
    void findAll() {
        final var users = userDao.findAll();

        assertThat(users).isNotEmpty();
    }

    @DisplayName("존재하는 id로 단건 조회 -> present")
    @Test
    void findById() {
        final var user = userDao.findById(1L);

        assertThat(user.get().getAccount()).isEqualTo("gugu");
    }

    @DisplayName("존재하지 않는 id로 단건 조회 -> empty")
    @Test
    void findById_InvalidUser() {
        assertThat(userDao.findById(2L)).isEmpty();
    }

    @DisplayName("존재하는 account로 단건 조회 -> present")
    @Test
    void findByAccount() {
        final var account = "gugu";
        final var user = userDao.findByAccount(account);

        assertThat(user.get().getAccount()).isEqualTo(account);
    }

    @DisplayName("존재하지 않는 account로 단건 조회 -> empty")
    @Test
    void findByAccount_InvalidAccount() {
        assertThat(userDao.findByAccount("not-exist")).isEmpty();
    }

    @DisplayName("유저 저장")
    @Test
    void insert() {
        final var account = "insert-gugu";
        final var user = new User(account, "password", "hkkang@woowahan.com");
        userDao.insert(user);

        final var actual = userDao.findById(2L).get();

        assertThat(actual.getAccount()).isEqualTo(account);
    }

    @DisplayName("유저 정보 update")
    @Test
    void update() {
        final var newPassword = "password99";
        final var user = userDao.findById(1L).get();
        user.changePassword(newPassword);

        userDao.update(user);

        final var actual = userDao.findById(1L).get();

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }
}
