package com.techcourse.dao;

import static org.assertj.core.api.Assertions.assertThat;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import javax.sql.DataSource;
import nextstep.jdbc.JdbcTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserDaoTest {

    private UserDao userDao;
    final DataSource dataSource = DataSourceConfig.getInstance();
    final JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
    private Long userId;

    @BeforeEach
    void setup() {
        DatabasePopulatorUtils.execute(dataSource);
        userDao = new UserDao(jdbcTemplate);
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userId = userDao.insert(user);
    }

    @AfterEach
    void tearDown() {
        initUsersTable(jdbcTemplate);
    }

    private void initUsersTable(final JdbcTemplate jdbcTemplate) {
        jdbcTemplate.update(connection -> connection.prepareStatement("truncate table users"));
        jdbcTemplate.update(connection ->
                connection.prepareStatement("alter table users alter column id restart with 1"));
    }

    @Test
    void findAll() {
        final var users = userDao.findAll();

        assertThat(users).isNotEmpty();
    }

    @Test
    void findById() {
        final var user = userDao.findById(userId);

        assertThat(user.getAccount()).isEqualTo("gugu");
    }

    @Test
    void findByAccount() {
        final var account = "gugu";
        final var user = userDao.findByAccount(account);

        assertThat(user.getAccount()).isEqualTo(account);
    }

    @Test
    void insert() {
        final var account = "insert-gugu";
        final var user = new User(account, "password", "hkkang@woowahan.com");
        final Long newUserId = userDao.insert(user);

        final var actual = userDao.findById(newUserId);

        assertThat(actual.getAccount()).isEqualTo(account);
    }

    @Test
    void update() {
        final var newPassword = "password99";
        final var user = userDao.findById(userId);
        user.changePassword(newPassword);

        userDao.update(user);

        final var actual = userDao.findById(userId);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }
}
