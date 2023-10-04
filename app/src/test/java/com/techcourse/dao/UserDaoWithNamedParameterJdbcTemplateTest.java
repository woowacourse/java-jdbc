package com.techcourse.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import java.util.Map;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.NamedParameterJdbcTemplate;

class UserDaoWithNamedParameterJdbcTemplateTest {

    private UserDaoWithNamedParameterJdbcTemplate userDao;
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @BeforeEach
    void setup() {
        final DataSource dataSource = DataSourceConfig.getInstance();
        DatabasePopulatorUtils.execute(dataSource);

        userDao = new UserDaoWithNamedParameterJdbcTemplate(dataSource);
        namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);

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
        final long savedUserId = userDao.findAll().get(0).getId();
        final var user = userDao.findById(savedUserId);

        assertThat(user.getAccount()).isEqualTo("gugu");
    }

    @Test
    void findByAccount() {
        final var account = "gugu";
        final var user = userDao.findByAccount(account);

        assertThat(user.getAccount()).isEqualTo(account);
    }

    @Test
    void findByAccountIncorrectColumnSize() {
        userDao.insert(new User("gugu", "password", "hkkang@woowahan.com"));

        assertThatThrownBy(() -> userDao.findByAccount("gugu"))
                .hasMessageContaining("결과가 1개인 줄 알았는데, 2개 나왔서!");
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
        final long savedUserId = userDao.findAll().get(0).getId();
        final var user = userDao.findById(savedUserId);
        user.changePassword(newPassword);

        userDao.update(user);

        final var actual = userDao.findById(savedUserId);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }

    @AfterEach
    void reset() {
        namedParameterJdbcTemplate.update("truncate table users", Map.of());
    }
}
