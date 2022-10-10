package com.techcourse.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import nextstep.jdbc.core.JdbcTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AppUserServiceTest {

    private JdbcTemplate jdbcTemplate;
    private UserDao userDao;

    @BeforeEach
    void setUp() {
        this.jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
        this.userDao = new UserDao(jdbcTemplate);

        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @Test
    void 회원을_아이디로_불러올_수_있다() {
        // given
        final var userHistoryDao = new UserHistoryDao(jdbcTemplate);
        final UserService userService = new AppUserService(userDao, userHistoryDao);

        // when
        final User actual = userService.findById(1L);

        // then
        assertAll(
                () -> assertThat(actual.getId()).isEqualTo(1L),
                () -> assertThat(actual).usingRecursiveComparison()
                        .ignoringFields("id")
                        .isEqualTo(new User("gugu", "password", "hkkang@woowahan.com"))
        );
    }

    @Test
    void 회원을_추가할_수_있다() {
        // given
        final var userHistoryDao = new UserHistoryDao(jdbcTemplate);
        final UserService userService = new AppUserService(userDao, userHistoryDao);
        final User user = new User("corinne", "password", "yoo77hyeon@gmail.com");

        // when
        userService.insert(user);

        // then
        final User found = userService.findById(2L);
        assertAll(
                () -> assertThat(found.getId()).isEqualTo(2L),
                () -> assertThat(found).usingRecursiveComparison()
                        .ignoringFields("id")
                        .isEqualTo(user)
        );
    }

    @Test
    void 비밀번호를_변경할_수_있다() {
        final var userHistoryDao = new UserHistoryDao(jdbcTemplate);
        final UserService userService = new AppUserService(userDao, userHistoryDao);

        final var newPassword = "qqqqq";
        final var createBy = "gugu";
        userService.changePassword(1L, newPassword, createBy);

        final var actual = userService.findById(1L);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }

    @AfterEach
    void setDown() {
        final JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
        final String userSql = "truncate table users";
        jdbcTemplate.update(userSql);
        final String userAlterSql = "alter table users alter column id restart with 1";
        jdbcTemplate.update(userAlterSql);

        final String historySql = "truncate table user_history";
        jdbcTemplate.update(historySql);
        final String historyAlterSql = "alter table user_history alter column id restart with 1";
        jdbcTemplate.update(historyAlterSql);
    }
}
