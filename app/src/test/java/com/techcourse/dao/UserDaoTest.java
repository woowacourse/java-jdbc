package com.techcourse.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import java.util.stream.Collectors;
import nextstep.jdbc.JdbcTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserDaoTest {

    private UserDao userDao;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setup() {
        final var dataSource = DataSourceConfig.getInstance();
        DatabasePopulatorUtils.execute(dataSource);

        jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.execute("TRUNCATE TABLE users");
        jdbcTemplate.execute("ALTER TABLE users ALTER COLUMN id RESTART WITH 1");
        userDao = new UserDao(jdbcTemplate);

        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @Test
    void findAll() {
        final User gugu = userDao.findByAccount("gugu").get();

        userDao.insert(new User("jjanggu", "password", "jjanggu@wooteco.com"));
        userDao.insert(new User("seona", "password", "seona@wooteco.com"));

        final User jjanggu = userDao.findByAccount("jjanggu").get();
        final User seona = userDao.findByAccount("seona").get();

        final var users = userDao.findAll()
                .stream()
                .map(User::getId)
                .collect(Collectors.toList());

        assertAll(
                () -> assertThat(users).hasSize(3),
                () -> assertThat(users).containsExactly(gugu.getId(), jjanggu.getId(), seona.getId())
        );
    }

    @Test
    void findById() {
        final var user = userDao.findById(1L).get();

        assertThat(user.getAccount()).isEqualTo("gugu");
    }

    @Test
    void findByAccount() {
        final var account = "gugu";
        final var user = userDao.findByAccount(account).get();

        assertThat(user.getAccount()).isEqualTo(account);
    }

    @Test
    void insert() {
        final var account = "insert-gugu";
        userDao.insert(new User(account, "password", "hkkang@woowahan.com"));

        final var user = userDao.findByAccount(account).get();

        assertAll(
                () -> assertThat(user.getId()).isEqualTo(2L),
                () -> assertThat(user.getAccount()).isEqualTo(account),
                () -> assertThat(user.getPassword()).isEqualTo("password"),
                () -> assertThat(user.getEmail()).isEqualTo("hkkang@woowahan.com")
        );
    }

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
