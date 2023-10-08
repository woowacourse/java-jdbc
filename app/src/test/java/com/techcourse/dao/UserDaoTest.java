package com.techcourse.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class UserDaoTest {

    private JdbcTemplate jdbcTemplate;
    private User user;
    private UserDao userDao;

    @BeforeEach
    void setup() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
        userDao = new UserDao(jdbcTemplate);

        userDao.insert(new User("gugu", "password", "hkkang@woowahan.com"));
        user = userDao.findByAccount("gugu");
    }

    @Test
    void 모든_회원을_조회한다() {
        // when
        final List<User> users = userDao.findAll();

        // then
        assertSoftly(softly -> {
            softly.assertThat(users).hasSize(1);
            final User user = users.get(0);

            softly.assertThat(user.getAccount()).isEqualTo("gugu");
            softly.assertThat(user.getPassword()).isEqualTo("password");
            softly.assertThat(user.getEmail()).isEqualTo("hkkang@woowahan.com");
        });
    }

    @Test
    void ID로_회원을_조회한다() {
        // when
        final User foundUser = userDao.findById(user.getId());

        // then
        assertSoftly(softly -> {
            softly.assertThat(foundUser.getAccount()).isEqualTo("gugu");
            softly.assertThat(foundUser.getPassword()).isEqualTo("password");
            softly.assertThat(foundUser.getEmail()).isEqualTo("hkkang@woowahan.com");
        });
    }

    @Test
    void 계정으로_회원을_조회한다() {
        // when
        final User foundUser = userDao.findByAccount(user.getAccount());

        // then
        assertSoftly(softly -> {
            softly.assertThat(foundUser.getAccount()).isEqualTo("gugu");
            softly.assertThat(foundUser.getPassword()).isEqualTo("password");
            softly.assertThat(foundUser.getEmail()).isEqualTo("hkkang@woowahan.com");
        });
    }

    @Test
    void 회원을_정보를_입력한다() {
        // given
        final var account = "huchu";
        final User huchu = new User(account, "password", "huchu@woowahan.com");

        // when
        userDao.insert(huchu);

        // then
        final User foundUser = userDao.findByAccount(account);
        assertSoftly(softly -> {
            softly.assertThat(foundUser.getAccount()).isEqualTo("huchu");
            softly.assertThat(foundUser.getPassword()).isEqualTo("password");
            softly.assertThat(foundUser.getEmail()).isEqualTo("huchu@woowahan.com");
        });
    }

    @Test
    void 회원_정보를_수정한다() {
        // given
        user.changePassword("newPassword");

        // when
        userDao.update(user);

        // then
        final User changedUser = userDao.findByAccount(user.getAccount());
        assertThat(changedUser.getPassword()).isEqualTo("newPassword");
    }

    @AfterEach
    void tearDown() {
        final String deleteSql = "DELETE FROM users";
        jdbcTemplate.update(deleteSql);
    }
}
