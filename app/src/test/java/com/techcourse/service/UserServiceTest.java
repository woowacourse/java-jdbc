package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserServiceTest {

    private static final long USER_ID = 1L;
    private static final String ORIGINAL_PASSWORD = "password";

    private JdbcTemplate jdbcTemplate;
    private UserDao userDao;

    @BeforeEach
    void setUp() {
        final var dataSource = new JdbcDataSource();
        dataSource.setUrl("jdbc:h2:mem:user-service-test;DB_CLOSE_DELAY=-1");
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.userDao = new UserDao(jdbcTemplate);

        DatabasePopulatorUtils.execute(dataSource);
        jdbcTemplate.update("truncate table user_history");
        jdbcTemplate.update("truncate table users restart identity");
        userDao.insert(new User("gugu", ORIGINAL_PASSWORD, "hkkang@woowahan.com"));
    }

    @Test
    @DisplayName("비밀번호 변경에 성공하면 새 비밀번호와 변경 이력이 함께 저장된다")
    void savesPasswordAndHistory() {
        // given
        final var userService = new UserService(userDao, new UserHistoryDao(jdbcTemplate));
        final var newPassword = "newPassword";
        final var changedBy = "operator";

        // when
        userService.changePassword(USER_ID, newPassword, changedBy);

        // then
        assertThat(userDao.findById(USER_ID).getPassword()).isEqualTo(newPassword);
        assertThat(findPasswordChanges()).singleElement().satisfies(change -> {
            assertThat(change.userId()).isEqualTo(USER_ID);
            assertThat(change.password()).isEqualTo(newPassword);
            assertThat(change.changedBy()).isEqualTo(changedBy);
            assertThat(change.changedAt()).isNotNull();
        });
    }

    @Test
    @Disabled("비밀번호 변경 트랜잭션을 적용할 때 활성화")
    @DisplayName("비밀번호 변경에 실패하면 이전 비밀번호가 유지된다")
    void rollsBackPassword() {
        // given
        final var userHistoryDao = new MockUserHistoryDao(jdbcTemplate);
        final var userService = new UserService(userDao, userHistoryDao);

        final var newPassword = "newPassword";

        // when
        assertThrows(DataAccessException.class,
                () -> userService.changePassword(USER_ID, newPassword, "operator"));

        // then
        assertThat(userDao.findById(USER_ID).getPassword()).isEqualTo(ORIGINAL_PASSWORD);
    }

    private List<PasswordChange> findPasswordChanges() {
        final var sql = "select user_id, password, created_by, created_at from user_history";
        return jdbcTemplate.query(sql, row -> new PasswordChange(
                row.getLong("user_id"),
                row.getString("password"),
                row.getString("created_by"),
                row.getObject("created_at", LocalDateTime.class)));
    }

    private record PasswordChange(long userId, String password, String changedBy, LocalDateTime changedAt) {
    }
}
