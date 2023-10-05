package com.techcourse.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

class UserDaoTest {

    private static final String INIT_USER_TABLE_SQL = "DROP TABLE IF EXISTS users; "
            + " "
            + "create table if not exists users ("
            + "    id bigint auto_increment,"
            + "    account varchar(100) not null,"
            + "    password varchar(100) not null,"
            + "    email varchar(100) not null,"
            + "    primary key(id)"
            + ");";

    private UserDao userDao;

    @BeforeEach
    void setup() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
        jdbcTemplate.execute(INIT_USER_TABLE_SQL);

        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        userDao = new UserDao(DataSourceConfig.getInstance());

        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
        List<User> all = userDao.findAll();
    }

    @Test
    void findAll() {
        final var users = userDao.findAll();

        assertThat(users).isNotEmpty();
    }

    @Test
    void findById() {
        final var user = userDao.findById(1L);

        assertThat(user.getAccount()).isEqualTo("gugu");
    }

    @Test
    void findById_fail() {
        assertThatThrownBy(
                () -> userDao.findById(-1L)
        ).isInstanceOf(NoSuchElementException.class)
                .hasMessage("id에 해당하는 user가 없습니다.");
    }

    @Test
    void findByAccount() {
        final var account = "gugu";
        final var user = userDao.findByAccount(account);

        assertThat(user.getAccount()).isEqualTo(account);
    }

    @Test
    void findByAccount_fail() {
        assertThatThrownBy(
                () -> userDao.findByAccount("joy")
        ).isInstanceOf(NoSuchElementException.class)
                .hasMessage("account에 해당하는 user가 없습니다.");
    }

    @Test
    void insert() {
        final var account = "insert-gugu";
        final var user = new User(account, "password", "hkkang@woowahan.com");
        userDao.insert(user);

        final var actual = userDao.findById(2L);

        assertThat(actual.getAccount()).isEqualTo(account);
    }

    @Test
    void update() {
        final var newPassword = "password99";
        final var user = userDao.findById(1L);
        user.changePassword(newPassword);

        userDao.update(user);

        final var actual = userDao.findById(1L);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }
}
