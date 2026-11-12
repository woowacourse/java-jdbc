package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

class UserServiceTest {

    private static final long USER_ID = 1L;
    private static final String ORIGINAL_PASSWORD = "password";

    private DataSource dataSource;
    private JdbcTemplate jdbcTemplate;
    private UserDao userDao;

    @BeforeEach
    void setUp() {
        final var testDataSource = new JdbcDataSource();
        testDataSource.setUrl("jdbc:h2:mem:user-service-test;DB_CLOSE_DELAY=-1");
        this.dataSource = testDataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.userDao = new UserDao(jdbcTemplate);

        DatabasePopulatorUtils.execute(dataSource);
        jdbcTemplate.update("truncate table user_history");
        jdbcTemplate.update("truncate table users restart identity");
        new UserService(dataSource).insert(new User("gugu", ORIGINAL_PASSWORD, "hkkang@woowahan.com"));
    }

    @Test
    @DisplayName("비밀번호 변경에 성공하면 새 비밀번호와 변경 이력이 함께 저장된다")
    void savesPasswordAndHistory() {
        // given
        final var userService = new UserService(dataSource);
        final var newPassword = "newPassword";
        final var changedBy = "operator";

        // when
        userService.changePassword(USER_ID, newPassword, changedBy);

        // then
        assertThat(userService.findById(USER_ID).getPassword()).isEqualTo(newPassword);
        assertThat(findPasswordChanges()).singleElement().satisfies(change -> {
            assertThat(change.userId()).isEqualTo(USER_ID);
            assertThat(change.password()).isEqualTo(newPassword);
            assertThat(change.changedBy()).isEqualTo(changedBy);
            assertThat(change.changedAt()).isNotNull();
        });
    }

    @Test
    @DisplayName("이력 저장 직후 실패하면 비밀번호 변경과 이력 저장이 모두 롤백된다")
    void rollsBackPasswordAndHistory() {
        // given
        final var historyFailure = new DataAccessException("이력 저장 직후 발생한 실패");
        final var userHistoryDao = new UserHistoryDao(jdbcTemplate) {
            @Override
            public void log(final Connection connection, final UserHistory history) {
                super.log(connection, history);
                throw historyFailure;
            }
        };
        final var userService = new UserService(userDao, userHistoryDao, dataSource);

        // when
        final var thrown = assertThrows(DataAccessException.class,
                () -> userService.changePassword(USER_ID, "newPassword", "operator"));

        // then
        assertThat(thrown).isSameAs(historyFailure);
        assertAll(
                () -> assertThat(userDao.findById(USER_ID).getPassword()).isEqualTo(ORIGINAL_PASSWORD),
                () -> assertThat(findPasswordChanges()).isEmpty());
    }

    @Test
    @DisplayName("이력 INSERT가 DB 제약을 위반하면 이전 비밀번호를 유지한다")
    void rollsBackPasswordWhenHistoryInsertFails() {
        // given
        final var userService = new UserService(dataSource);

        // when
        final var thrown = assertThrows(DataAccessException.class,
                () -> userService.changePassword(USER_ID, "newPassword", null));

        // then
        assertThat(thrown).hasCauseInstanceOf(SQLIntegrityConstraintViolationException.class);
        assertThat(userDao.findById(USER_ID).getPassword()).isEqualTo(ORIGINAL_PASSWORD);
        assertThat(findPasswordChanges()).isEmpty();
    }

    @Test
    @DisplayName("커밋 뒤 연결 종료에 실패하면 변경이 이미 저장됐음을 전달한다")
    void reportsConnectionCloseFailureAfterCommit() throws SQLException {
        // given
        final var closeFailure = new SQLException("연결 종료 실패");
        final var connection = spy(dataSource.getConnection());
        doAnswer(invocation -> {
            invocation.callRealMethod();
            throw closeFailure;
        }).when(connection).close();
        final var failingDataSource = mock(DataSource.class);
        when(failingDataSource.getConnection()).thenReturn(connection);
        final var userService = new UserService(failingDataSource);
        final var newPassword = "newPassword";

        // when
        final var thrown = assertThrows(PasswordChangeCommittedException.class,
                () -> userService.changePassword(USER_ID, newPassword, "operator"));

        // then
        assertThat(thrown).hasCause(closeFailure).hasMessageContaining("커밋");
        assertThat(userDao.findById(USER_ID).getPassword()).isEqualTo(newPassword);
        assertThat(findPasswordChanges()).extracting(PasswordChange::password).containsExactly(newPassword);
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
