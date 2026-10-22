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

    @BeforeEach
    void setup() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());

        final var jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
        jdbcTemplate.update("truncate table users restart identity");

        userDao = new UserDao(jdbcTemplate);
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @Test
    void 모든_사용자를_조회한다() {
        userDao.insert(new User("other-gugu", "password", "other@woowahan.com"));

        final var users = userDao.findAll();

        assertThat(users)
                .extracting(User::getAccount)
                .containsExactlyInAnyOrder("gugu", "other-gugu");
    }

    @Test
    void id로_사용자를_조회한다() {
        final var user = userDao.findById(1L).orElseThrow();

        assertThat(user.getAccount()).isEqualTo("gugu");
    }

    @Test
    void 존재하지_않는_id로_조회하면_빈_Optional을_반환한다() {
        final var user = userDao.findById(999L);

        assertThat(user).isEmpty();
    }

    @Test
    void 계정으로_사용자를_조회한다() {
        final var account = "gugu";
        final var user = userDao.findByAccount(account).orElseThrow();

        assertThat(user.getAccount()).isEqualTo(account);
    }

    @Test
    void 존재하지_않는_계정으로_조회하면_빈_Optional을_반환한다() {
        final var user = userDao.findByAccount("unknown");

        assertThat(user).isEmpty();
    }

    @Test
    void 사용자를_저장한다() {
        final var account = "insert-gugu";
        final var user = new User(account, "password", "hkkang@woowahan.com");
        userDao.insert(user);

        final var actual = userDao.findById(2L).orElseThrow();

        assertThat(actual.getAccount()).isEqualTo(account);
    }

    @Test
    void 사용자_정보를_수정한다() {
        final var newPassword = "password99";
        final var user = userDao.findById(1L).orElseThrow();
        user.changePassword(newPassword);

        userDao.update(user);

        final var actual = userDao.findById(1L).orElseThrow();

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }

    @Test
    void 사용자_정보를_수정해도_다른_사용자는_변경되지_않는다() {
        userDao.insert(new User("other-gugu", "password", "other@woowahan.com"));
        final var user = userDao.findById(1L).orElseThrow();
        user.changePassword("password99");

        userDao.update(user);

        final var other = userDao.findById(2L).orElseThrow();
        assertThat(other.getPassword()).isEqualTo("password");
    }
}
