package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.time.LocalDateTime;
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
    void change_password() throws SQLException {
        final var userHistoryDao = new UserHistoryDao(dataSource);
        final var userService = new UserService(dataSource, userDao, userHistoryDao);

        final var newPassword = "qqqqq";
        final var createBy = "gugu";
        final var before = LocalDateTime.now().minusSeconds(1);
        userService.changePassword(1L, newPassword, createBy);

        final var actual = userService.findById(1L);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
        try (final var connection = dataSource.getConnection();
             final var statement = connection.prepareStatement("select * from user_history");
             final var resultSet = statement.executeQuery()) {
            assertThat(resultSet.next()).isTrue();
            assertThat(resultSet.getLong("user_id")).isEqualTo(1L);
            assertThat(resultSet.getString("account")).isEqualTo(actual.getAccount());
            assertThat(resultSet.getString("password")).isEqualTo(newPassword);
            assertThat(resultSet.getString("email")).isEqualTo(actual.getEmail());
            assertThat(resultSet.getString("created_by")).isEqualTo(createBy);
            assertThat(resultSet.getObject("created_at", LocalDateTime.class))
                    .isBetween(before, LocalDateTime.now().plusSeconds(1));
            assertThat(resultSet.next()).isFalse();
        }
    }

    @Test
    @DisplayName("이력 저장 후 실패하면 비밀번호와 이력을 모두 롤백한다")
    void rollback_password_and_history() {
        final var userHistoryDao = new MockUserHistoryDao(dataSource);
        final var userService = new UserService(dataSource, userDao, userHistoryDao);

        final var newPassword = "newPassword";
        final var createBy = "gugu";
        assertThatThrownBy(() -> userService.changePassword(1L, newPassword, createBy))
                .isInstanceOf(DataAccessException.class);

        final var actual = userService.findById(1L);

        assertThat(actual.getPassword()).isEqualTo("password");
        assertThat(historyCount()).isZero();
    }

    @Test
    @DisplayName("이력 INSERT가 DB 제약 조건을 위반하면 비밀번호 변경도 롤백한다")
    void rollback_history_constraint_violation() {
        final var userService = new UserService(dataSource, userDao, new UserHistoryDao(dataSource));

        assertThatThrownBy(() -> userService.changePassword(1L, "newPassword", null))
                .isInstanceOf(DataAccessException.class)
                .hasCauseInstanceOf(SQLException.class);

        assertThat(userDao.findById(1L).getPassword()).isEqualTo("password");
        assertThat(historyCount()).isZero();
    }

    @Test
    @DisplayName("비밀번호를 변경할 사용자가 없으면 아이디를 담은 예외를 던진다")
    void change_password_for_missing_user() {
        final var userService = new UserService(dataSource, userDao, new UserHistoryDao(dataSource));

        assertThatThrownBy(() -> userService.changePassword(999L, "newPassword", "gugu"))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("999");
        assertThat(userDao.findById(1L).getPassword()).isEqualTo("password");
        assertThat(historyCount()).isZero();
    }

    private long historyCount() {
        return jdbcTemplate.queryForObject("select count(*) from user_history", row -> row.getLong(1));
    }
}
