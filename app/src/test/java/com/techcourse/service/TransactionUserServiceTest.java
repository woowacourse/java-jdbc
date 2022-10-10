package com.techcourse.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import nextstep.jdbc.core.JdbcTemplate;
import nextstep.jdbc.exception.DataAccessException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TransactionUserServiceTest {

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
    void 회원_추가_시_예외가_발생하면_작업이_롤백된다() {
        // given
        final var userHistoryDao = new UserHistoryDao(jdbcTemplate);
        final var mockUserDao = new MockUserDao(jdbcTemplate);
        final UserService userService = new TransactionUserService(new AppUserService(mockUserDao, userHistoryDao));
        final User user = new User("corinne", "password", "yoo77hyeon@gmail.com");

        // when, then
        assertAll(
                () -> assertThatThrownBy(() -> userService.insert(user)).isInstanceOf(DataAccessException.class),
                () -> assertThat(userService.findById(2L)).isNull()
        );
    }

    @Test
    void 비밀번호_변경_시_예외가_발생하면_모든_작업이_롤백된다() {
        // given
        final var userHistoryDao = new MockUserHistoryDao(jdbcTemplate);
        final UserService userService = new TransactionUserService(new AppUserService(userDao, userHistoryDao));

        final var newPassword = "newPassword";
        final var createBy = "gugu";

        // when, then
        assertAll(
                () -> assertThrows(DataAccessException.class,
                        () -> userService.changePassword(1L, newPassword, createBy)),
                () -> assertThat(userService.findById(1L)
                        .getPassword()).isNotEqualTo(newPassword)
        );
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
