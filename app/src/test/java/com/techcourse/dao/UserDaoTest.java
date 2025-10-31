package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserDaoTest {

    private UserDao userDao;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setup() {
        var dataSource = DataSourceConfig.getInstance();
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.update("DROP TABLE IF EXISTS users");
        DatabasePopulatorUtils.execute(dataSource);
        userDao = new UserDao(jdbcTemplate);

        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @Test
    void findAll() {
        final var users = userDao.findAll();

        assertThat(users).isNotEmpty();
    }

    @Test
    void findById() {
        assertThat(userDao.findById(1L))
                .isPresent()
                .hasValueSatisfying(user ->
                        assertThat(user.getAccount()).isEqualTo("gugu")
                );
    }

    @Test
    void findByAccount() {
        final var account = "gugu";
        assertThat(userDao.findByAccount(account))
                .isPresent()
                .hasValueSatisfying(user ->
                        assertThat(user.getAccount()).isEqualTo(account)
                );
    }

    @Test
    void insert() {
        final var account = "insert-gugu";
        final var user = new User(account, "password", "hkkang@woowahan.com");
        userDao.insert(user);

        assertThat(userDao.findByAccount(account))
                .isPresent()
                .hasValueSatisfying(actual ->
                        assertThat(actual.getAccount()).isEqualTo(account)
                );
    }

    @Test
    void update() {
        final var newPassword = "password99";
        final var user = userDao.findByAccount("gugu").orElseThrow();
        user.changePassword(newPassword);

        userDao.update(user);

        assertThat(userDao.findByAccount("gugu"))
                .isPresent()
                .hasValueSatisfying(actual ->
                        assertThat(actual.getPassword()).isEqualTo(newPassword)
                );
    }

    @Test
    void findByAccount_withPreparedStatementSetter() {
        final var account = "gugu";

        assertThat(userDao.findByAccountWithPss(account))
                .isPresent()
                .hasValueSatisfying(user ->
                        assertThat(user.getAccount()).isEqualTo(account)
                );
    }
}
