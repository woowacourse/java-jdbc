package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserServiceTest {

    private DataSource dataSource;
    private JdbcTemplate jdbcTemplate;
    private UserDao userDao;

    @BeforeEach
    void setUp() {
        this.dataSource = DataSourceConfig.getInstance();
        DatabasePopulatorUtils.execute(dataSource);
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.update("truncate table user_history restart identity");
        jdbcTemplate.update("truncate table users restart identity");

        this.userDao = new UserDao(dataSource);
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @Test
    @DisplayName("비밀번호 변경과 변경자, 변경 시각을 담은 이력을 함께 저장한다")
    void change_password() {
        final var userService = UserServiceFactory.create(dataSource);

        final var newPassword = "qqqqq";
        final var createBy = "gugu";
        final var before = LocalDateTime.now().minusSeconds(1);
        userService.changePassword(1L, newPassword, createBy);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();

        final var actual = userService.findById(1L);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
        final var histories = findHistories();
        assertThat(histories).hasSize(1);
        final var history = histories.getFirst();
        assertThat(history.userId()).isEqualTo(1L);
        assertThat(history.account()).isEqualTo("gugu");
        assertThat(history.password()).isEqualTo(newPassword);
        assertThat(history.email()).isEqualTo("hkkang@woowahan.com");
        assertThat(history.createdBy()).isEqualTo(createBy);
        assertThat(history.createdAt()).isBetween(before, LocalDateTime.now().plusSeconds(1));
    }

    @Test
    @DisplayName("이력 저장 후 실패하면 비밀번호와 이력을 모두 롤백한다")
    void rollback_password_and_history() {
        final var userHistoryDao = new MockUserHistoryDao(dataSource);
        final var userService = new TxUserService(new AppUserService(userDao, userHistoryDao), dataSource);

        final var newPassword = "newPassword";
        final var createBy = "gugu";
        assertThatThrownBy(() -> userService.changePassword(1L, newPassword, createBy))
                .isInstanceOf(DataAccessException.class);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();

        final var actual = userService.findById(1L);

        assertThat(actual.getPassword()).isEqualTo("password");
        assertThat(findHistories()).isEmpty();
    }

    @Test
    @DisplayName("다음 비밀번호 변경이 실패해도 직전 비밀번호와 기존 이력은 유지한다")
    void preserve_previous_password_and_history_after_failure() {
        final var userService = UserServiceFactory.create(dataSource);
        final var committedPassword = "committed-password";
        userService.changePassword(1L, committedPassword, "first-editor");
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();

        final var historiesBefore = findHistories();
        assertThat(historiesBefore).hasSize(1);

        final var failingService = new TxUserService(new AppUserService(userDao, new MockUserHistoryDao(dataSource)), dataSource);

        assertThatThrownBy(() -> failingService.changePassword(1L, "failed-password", "second-editor"))
                .isInstanceOf(DataAccessException.class);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();

        assertThat(userDao.findById(1L).getPassword()).isEqualTo(committedPassword);
        assertThat(findHistories())
                .containsExactlyElementsOf(historiesBefore);
    }

    @Test
    @DisplayName("이력 INSERT가 DB 제약 조건을 위반하면 비밀번호 변경도 롤백한다")
    void rollback_history_constraint_violation() {
        final var userService = UserServiceFactory.create(dataSource);

        assertThatThrownBy(() -> userService.changePassword(1L, "newPassword", null))
                .isInstanceOf(DataAccessException.class)
                .hasCauseInstanceOf(SQLException.class);
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();

        assertThat(userDao.findById(1L).getPassword()).isEqualTo("password");
        assertThat(findHistories()).isEmpty();
    }

    @Test
    @DisplayName("비밀번호를 변경할 사용자가 없으면 아이디를 담은 예외를 던진다")
    void change_password_for_missing_user() {
        final var userService = UserServiceFactory.create(dataSource);

        assertThatThrownBy(() -> userService.changePassword(999L, "newPassword", "gugu"))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("999");
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
        assertThat(userDao.findById(1L).getPassword()).isEqualTo("password");
        assertThat(findHistories()).isEmpty();
    }

    private List<HistorySnapshot> findHistories() {
        return jdbcTemplate.query("select * from user_history order by id", row -> new HistorySnapshot(
                row.getLong("id"), row.getLong("user_id"), row.getString("account"),
                row.getString("password"), row.getString("email"),
                row.getObject("created_at", LocalDateTime.class), row.getString("created_by")));
    }

    private record HistorySnapshot(long id, long userId, String account, String password, String email,
                                   LocalDateTime createdAt, String createdBy) {}
}
