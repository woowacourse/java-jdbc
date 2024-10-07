package com.techcourse.dao;

import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;

import static org.assertj.core.api.Assertions.assertThat;

class UserDaoTest {

    private UserDao userDao;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setup() {
        DataSource dataSource = DataSourceConfig.getInstance();
        jdbcTemplate = new JdbcTemplate(dataSource);
        userDao = new UserDao(jdbcTemplate);
        jdbcTemplate.update("DROP TABLE IF EXISTS users");
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        User user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @DisplayName("모든 사용자의 정보를 조회한다.")
    @Test
    void findAll() {
        // when & then
        List<User> users = userDao.findAll();

        assertThat(users).isNotEmpty();
    }

    @DisplayName("식별자로 사용자를 조회한다.")
    @Test
    void findById() {
        // when & then
        Optional<User> user = userDao.findById(1L);

        assertThat(user.get().getAccount()).isEqualTo("gugu");
    }

    @DisplayName("계정으로 사용자를 조회한다.")
    @Test
    void findByAccount() {
        // given
        String account = "gugu";

        // when
        Optional<User> user = userDao.findByAccount(account);

        // then
        assertThat(user.get().getAccount()).isEqualTo(account);
    }

    @DisplayName("새로운 사용자를 저장한다.")
    @Test
    void insert() {
        // given
        String account = "insert-gugu";
        User user = new User(account, "password", "hkkang@woowahan.com");

        // when
        userDao.insert(user);

        // then
        Optional<User> result = userDao.findById(2L);

        assertThat(result.get().getAccount()).isEqualTo(account);
    }

    @DisplayName("사용자의 정보를 변경한다.")
    @Test
    void update() {
        // given
        String newPassword = "password99";
        User user = userDao.findById(1L).get();
        user.changePassword(newPassword);

        // when
        userDao.update(user);

        // then
        Optional<User> result = userDao.findById(1L);

        assertThat(result.get().getPassword()).isEqualTo(newPassword);
    }
}
