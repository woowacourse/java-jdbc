package com.techcourse.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AppUserServiceTest {

    private JdbcTemplate jdbcTemplate;
    private UserDao userDao;
    private AppUserService appUserService;

    @BeforeEach
    void setUp() {
        this.jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
        this.userDao = new UserDao();
        this.appUserService = new AppUserService(userDao, new UserHistoryDao());

        DataSource dataSource = DataSourceConfig.getInstance();
        DatabasePopulatorUtils.execute(dataSource);

        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.executeUpdate("""
                delete from users;
                alter table users alter column id restart with 1;
                """);
    }

    @DisplayName("성공: User 저장 후 id로 조회")
    @Test
    void insert_findById() {
        appUserService.insert(new User("tre", "pass123", "a@a.com"));

        User user = appUserService.findById(2L)
                .orElseThrow();

        assertAll(
                () -> assertThat(user.getId()).isEqualTo(2L),
                () -> assertThat(user.getAccount()).isEqualTo("tre"),
                () -> assertThat(user.getPassword()).isEqualTo("pass123"),
                () -> assertThat(user.getEmail()).isEqualTo("a@a.com")
        );
    }

    @DisplayName("성공: User 저장 후 findByAccount로 조회")
    @Test
    void insert_findByAccount() {
        appUserService.insert(new User("tre", "pass123", "a@a.com"));

        User user = appUserService.findByAccount("tre")
                .orElseThrow();

        assertAll(
                () -> assertThat(user.getId()).isEqualTo(2L),
                () -> assertThat(user.getAccount()).isEqualTo("tre"),
                () -> assertThat(user.getPassword()).isEqualTo("pass123"),
                () -> assertThat(user.getEmail()).isEqualTo("a@a.com")
        );
    }

    @DisplayName("성공: 비밀번호 변경")
    @Test
    void changePassword() throws SQLException {
        final var userHistoryDao = new UserHistoryDao();
        final var userService = new AppUserService(userDao, userHistoryDao);

        final var newPassword = "qqqqq";
        final var createBy = "gugu";
        userService.changePassword(1L, newPassword, createBy);

        final var actual = userService.findById(1L).get();

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }

    @DisplayName("실패: 존재하지 않는 사용자가 비밀번호 변경")
    @Test
    void changePassword_InvalidUser() {
        final var userHistoryDao = new UserHistoryDao();
        final var userService = new AppUserService(userDao, userHistoryDao);

        final var newPassword = "qqqqq";
        final var createBy = "gugu";

        assertThatThrownBy(() -> userService.changePassword(2L, newPassword, createBy))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("changePassword() 메서드에서 트랜잭션이 정상 동작한다.")
    @Test
    void testTransactionRollback() {
        // 트랜잭션 롤백 테스트를 위해 mock으로 교체
        final var userHistoryDao = new MockUserHistoryDao(jdbcTemplate);
        final var userService = new AppUserService(userDao, userHistoryDao);
        final var txUserService = new TransactionalUserService(userService);

        final var newPassword = "newPassword";
        final var createBy = "gugu";
        // 트랜잭션이 정상 동작하는지 확인하기 위해 의도적으로 MockUserHistoryDao에서 예외를 발생시킨다.
        assertThrows(DataAccessException.class,
                () -> txUserService.changePassword(1L, newPassword, createBy));

        final var actual = userService.findById(1L).get();

        assertThat(actual.getPassword()).isNotEqualTo(newPassword);
    }
}
