package com.techcourse.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class UserServiceTest {

    private JdbcTemplate jdbcTemplate;
    private UserDao userDao;

    @BeforeEach
    void setUp() {
        this.jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
        this.userDao = new UserDao(jdbcTemplate);

        jdbcTemplate.update("drop table if exists user_history");
        jdbcTemplate.update("drop table if exists users");
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        User user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @Test
    void 비밀번호를_변경하면_새_비밀번호가_조회된다() {
        UserHistoryDao userHistoryDao = new UserHistoryDao(jdbcTemplate);
        UserService userService = new UserService(userDao, userHistoryDao);

        String newPassword = "qqqqq";
        String createBy = "gugu";
        userService.changePassword(1L, newPassword, createBy);

        User actual = userService.findById(1L).orElseThrow();

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }

    @Disabled("3단계에서 트랜잭션 롤백 구현")
    @Test
    void 이력_저장에_실패하면_비밀번호_변경도_취소된다() {
        UserHistoryDao userHistoryDao = new MockUserHistoryDao(jdbcTemplate);
        UserService userService = new UserService(userDao, userHistoryDao);

        String newPassword = "newPassword";
        String createBy = "gugu";
        assertThrows(DataAccessException.class,
                () -> userService.changePassword(1L, newPassword, createBy));

        User actual = userService.findById(1L).orElseThrow();

        assertThat(actual.getPassword()).isNotEqualTo(newPassword);
    }

    @Test
    void 존재하지_않는_사용자를_조회하면_값이_없음을_반환한다() {
        UserService service = new UserService(userDao, new UserHistoryDao(jdbcTemplate));
        assertThat(service.findById(999L)).isEmpty();
    }

    @Test
    void 존재하지_않는_사용자의_비밀번호를_변경하면_예외를_던진다() {
        UserService service = new UserService(userDao, new UserHistoryDao(jdbcTemplate));
        assertThrows(UserNotFoundException.class, () -> service.changePassword(999L, "new", "admin"));
        assertThat(jdbcTemplate.queryForObject("select count(*) from user_history", row -> row.getLong(1)))
            .contains(0L);
    }

    @Test
    void 조회한_사용자가_수정되지_않으면_예외를_던지고_저장하지_않는다() {
        // given
        UserDao userDao = mock(UserDao.class);
        UserHistoryDao userHistoryDao = mock(UserHistoryDao.class);
        UserService service = new UserService(userDao, userHistoryDao);
        User user = new User(1L, "gugu", "password", "gugu@example.com");
        given(userDao.findById(1L)).willReturn(Optional.of(user));
        given(userDao.update(user)).willReturn(0);

        // when
        Throwable exception = catchThrowable(
            () -> service.changePassword(1L, "changed", "admin")
        );

        // then
        assertThat(exception).isInstanceOf(UserNotFoundException.class);
        then(userDao).should().update(user);
        then(userHistoryDao).should(never()).log(any(UserHistory.class));
    }
}
