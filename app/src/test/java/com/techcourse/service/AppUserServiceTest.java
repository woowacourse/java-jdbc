package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AppUserServiceTest {

    private JdbcTemplate jdbcTemplate;
    private UserDao userDao;
    private User testUser;

    @BeforeEach
    void setUp() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());

        this.jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
        this.userDao = new UserDao(jdbcTemplate);

        // 테스트 데이터 초기화
        jdbcTemplate.update("DELETE FROM USERS");

        // 테스트용 사용자 생성 및 저장
        final var user = new User("gugu", "password", "gugu@email.com");
        userDao.insert(user);

        // 저장된 사용자 조회 (ID 포함)
        this.testUser = userDao.findByAccount("gugu");
    }

    @Test
    void changePassword() {
        // given
        final var userHistoryDao = new UserHistoryDao(jdbcTemplate);
        final var appUserService = new AppUserService(userDao, userHistoryDao);
        final var userService = new TxUserService(appUserService);

        final var newPassword = "newPassword123";
        final var createdBy = "gugu";

        // when
        userService.changePassword(testUser.getId(), newPassword, createdBy);

        // then
        final var updatedUser = userService.findById(testUser.getId());
        assertThat(updatedUser.getPassword()).isEqualTo(newPassword);
    }

    @Test
    void transactionRollback() {
        // given
        final var userHistoryDao = new MockUserHistoryDao(jdbcTemplate);
        final var appUserService = new AppUserService(userDao, userHistoryDao);
        final var userService = new TxUserService(appUserService);

        final var originalPassword = testUser.getPassword();
        final var newPassword = "newPassword123";
        final var createdBy = "gugu";

        // when & then
        // MockUserHistoryDao에서 의도적으로 예외 발생 → 트랜잭션 롤백
        assertThrows(DataAccessException.class,
                () -> userService.changePassword(testUser.getId(), newPassword, createdBy));

        // 롤백 확인: 비밀번호가 변경되지 않아야 함
        final var unchangedUser = userService.findById(testUser.getId());
        assertThat(unchangedUser.getPassword()).isEqualTo(originalPassword);
        assertThat(unchangedUser.getPassword()).isNotEqualTo(newPassword);
    }
}
